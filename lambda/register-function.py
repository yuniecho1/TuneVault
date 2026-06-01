import json
import boto3

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('login')

def lambda_handler(event, context):

    # handle both API + test
    if 'body' in event:
        body = json.loads(event['body'])
    else:
        body = event

    email = body['email']
    username = body['user_name']
    password = body['password']

    # check if user exists
    response = table.get_item(Key={'email': email})

    if 'Item' in response:
        return {
            'statusCode': 400,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps("Email already exists")
        }

    # insert new user
    table.put_item(
        Item={
            'email': email,
            'user_name': username,
            'password': password
        }
    )

    return {
        'statusCode': 200,
        'headers': {"Access-Control-Allow-Origin": "*"},
        'body': json.dumps("User registered successfully")
    }