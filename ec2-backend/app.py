from flask import Flask, request, jsonify
from flask_cors import CORS
import boto3
from boto3.dynamodb.conditions import Key
from decimal import Decimal

app = Flask(__name__)
CORS(app)

dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
login_table = dynamodb.Table('login')
music_table = dynamodb.Table('music')
subs_table = dynamodb.Table('subscribe')

def convert_decimals(obj):
    if isinstance(obj, list):
        return [convert_decimals(i) for i in obj]
    elif isinstance(obj, dict):
        return {k: convert_decimals(v) for k, v in obj.items()}
    elif isinstance(obj, Decimal):
        return int(obj)
    else:
        return obj


@app.route('/login', methods=['POST'])
def login():
    body = request.get_json()

    email = body['email']
    password = body['password']

    response = login_table.get_item(Key={'email': email})

    if 'Item' not in response:
        return jsonify('User not found'), 401

    user = response['Item']

    if user['password'] != password:
        return jsonify('Wrong password'), 401

    return jsonify({
        'message': 'Login success',
        'user_name': user['user_name']
    }), 200


@app.route('/register', methods=['POST'])
def register():
    body = request.get_json()

    email = body['email']
    username = body['user_name']
    password = body['password']

    # check if user exists
    response = login_table.get_item(Key={'email': email})

    if 'Item' in response:
        return jsonify('Email already exists'), 400

    # insert new user
    login_table.put_item(
        Item={
            'email': email,
            'user_name': username,
            'password': password
        }
    )

    return jsonify('User registered successfully'), 200


@app.route('/music', methods=['GET'])
def query_music():
    params = request.args

    artist = params.get('artist')
    album = params.get('album')
    year = params.get('year')
    title = params.get('title')

    if artist:
        response = music_table.query(
            KeyConditionExpression=Key('artist').eq(artist)
        )
        items = response['Items']
    else:
        response = music_table.scan()
        items = response['Items']

    results = []
    for item in items:
        if album and item.get('album') != album:
            continue
        if year and item.get('year') != year:
            continue
        if title and title.lower() not in item.get('title', '').lower():
            continue
        results.append(item)

    return jsonify(convert_decimals(results)), 200


#GET - get all subscriptions for a user
@app.route('/subscribe', methods=['GET'])
def get_subscriptions():
    email = request.args.get('email')

    if not email:
        return jsonify('Missing email'), 400

    response = subs_table.query(
        KeyConditionExpression=Key('email').eq(email)
    )

    return jsonify(convert_decimals(response['Items'])), 200


#POST - subscribe to a song
@app.route('/subscribe', methods=['POST'])
def subscribe():
    body = request.get_json()

    email = body.get('email')
    artist = body.get('artist')
    title = body.get('title')
    album = body.get('album')
    year = body.get('year')
    image_url = body.get('image_url')

    # check missing fields
    if not email or not artist or not title or not album:
        return jsonify('Missing required fields'), 400

    # validate song exists in music table
    song_id_music = f"{album}#{title}"
    response = music_table.get_item(
        Key={'artist':artist, 'album_title': song_id_music}
    )

    if 'Item' not in response:
            return jsonify('Invalid song'), 400

    # same logic as subscribe table
    song_id = f"{artist.lower()}#{album.lower()}#{title.lower()}"

    try:
        subs_table.put_item(
            Item={
                'email': email,
                'song_id': song_id,
                'artist': artist,
                'title': title,
                'album': album,
                'year': year,
                'image_url': image_url
            },
            ConditionExpression="attribute_not_exists(song_id)"
        )

        return jsonify('Subscribed successfully'), 200

    except dynamodb.meta.client.exceptions.ConditionalCheckFailedException:
        return jsonify('Already subscribed'), 400


@app.route('/subscribe', methods=['DELETE'])
def remove_subscription():
    email = request.args.get('email')
    song_id = request.args.get('song_id')

    if not email or not song_id:
        return jsonify('Missing required fields'), 400

    # same logic as subscribe
    subs_table.delete_item(
        Key={
            'email': email,
            'song_id': song_id
        }
    )

    return jsonify('Removed successfully'), 200


@app.route('/', methods=['GET'])
def health():
    return jsonify({'status': 'running'}), 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)