import json
import boto3
import botocore
from boto3.dynamodb.conditions import Key
from decimal import Decimal

dynamodb = boto3.resource('dynamodb')
subs_table = dynamodb.Table('subscribe')   
music_table = dynamodb.Table('music')

def convert_decimals(obj):
    if isinstance(obj, list):
        return [convert_decimals(i) for i in obj]
    elif isinstance(obj, dict):
        return {k: convert_decimals(v) for k, v in obj.items()}
    elif isinstance(obj, Decimal):
        return int(obj)
    else:
        return obj
    
def lambda_handler(event, context):
    http_method = event.get('httpMethod', 'POST')

    #GET - get all subscriptions for a user
    if http_method == 'GET':
        params = event.get('queryStringParameters') or {}
        email = params.get('email')

        if not email:
            return {
                'statusCode': 400,
                'headers': {"Access-Control-Allow-Origin": "*"},
                'body': json.dumps("Missing email")
            }

        response = subs_table.query(
            KeyConditionExpression=Key('email').eq(email)
        )

        return {
            'statusCode': 200,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps(convert_decimals(response['Items']))
        }
    
    # POST - subscribe to a song
    if 'body' in event:
        body = json.loads(event['body'])
    else:
        body = event

    email = body.get('email')
    artist = body.get('artist')
    title = body.get('title')
    album = body.get('album')
    year = body.get('year')
    image_url = body.get('image_url')

    # check missing fields
    if not email or not artist or not title or not album:
        return {
            'statusCode': 400,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps("Missing required fields")
        }

    # validate song exists in music table (using get_item instead of scan)
    song_id_music = f"{album}#{title}"
    response = music_table.get_item(
        Key={'artist':artist, 'album_title': song_id_music}
    )

    if 'Item' not in response:
            return {
                'statusCode': 400,
                'headers': {"Access-Control-Allow-Origin": "*"},
                'body': json.dumps("Invalid song")
            }
    
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

        return {
            'statusCode': 200,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps("Subscribed successfully")
        }
    
    except botocore.exceptions.ClientError as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            return {
                'statusCode': 400,
                'headers': {"Access-Control-Allow-Origin": "*"},
                'body': json.dumps("Already subscribed")
            }
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps(f"Error: {str(e)}")
        }