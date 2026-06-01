import json
import boto3

dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table('login')

def lambda_handler(event, context):

    # handle both API Gateway + Lambda test
    if 'body' in event:
        body = json.loads(event['body'])
    else:
        body = event

    email = body['email']
    password = body['password']

    response = table.get_item(Key={'email': email})

    if 'Item' not in response:
        return {
            'statusCode': 401,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps('User not found')
        }

    user = response['Item']

    if user['password'] != password:
        return {
            'statusCode': 401,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps('Wrong password')
        }

    return {
        'statusCode': 200,
        'headers': {"Access-Control-Allow-Origin": "*"},
        'body': json.dumps({
            'message': 'Login success',
            'user_name': user['user_name']
        })
    }