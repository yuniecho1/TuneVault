import json
import boto3

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('subscribe')  


def lambda_handler(event, context):

    if 'body' in event:
        body = json.loads(event['body'])
    else:
        body = event

    email = body.get('email')
    artist = body.get('artist')
    title = body.get('title')
    album = body.get('album')

    if not email or not artist or not title or not album:
        return {
            'statusCode': 400,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps("Missing required fields")
        }

    # same logic as subscribe
    song_id = f"{artist.lower()}#{album.lower()}#{title.lower()}"

    table.delete_item(
        Key={
            'email': email,
            'song_id': song_id
        }
    )

    return {
        'statusCode': 200,
        'headers': {"Access-Control-Allow-Origin": "*"},
        'body': json.dumps("Removed successfully")
    }