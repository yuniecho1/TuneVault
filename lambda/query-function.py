import json
import boto3
from boto3.dynamodb.conditions import Key
from decimal import Decimal

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('music')

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
    params = event.get('queryStringParameters') or {}
    
    artist = params.get('artist')
    album = params.get('album')
    year = params.get('year')
    title = params.get('title')

    if artist:
        response = table.query(
            KeyConditionExpression=Key('artist').eq(artist)
        )
        items = response['Items']
    else:
        response = table.scan()
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

    clean_results = convert_decimals(results)

    return {
        'statusCode': 200,
        'headers': {"Access-Control-Allow-Origin": "*"},
        'body': json.dumps(clean_results)
    }