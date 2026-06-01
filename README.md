# # Tune Vault — AWS Lambda Functions

A cloud-based music subscription app backed by AWS Lambda and DynamoDB. Users can register, log in, search for songs, and manage a personal subscription list.

---

## Project Structure

```
├── login-function.py       # Authenticates a user
├── register-function.py    # Creates a new user account
├── query-function.py       # Searches the music catalogue
├── subscribe-function.py   # Subscribes/retrieves a user's songs
├── remove-function.py      # Removes a subscription
```

---

## DynamoDB Tables

### `login`
Stores user credentials.

| Attribute   | Type   | Key          |
|-------------|--------|--------------|
| `email`     | String | Partition key |
| `user_name` | String |              |
| `password`  | String |              |

### `music`
Stores the song catalogue.

| Attribute     | Type   | Key          |
|---------------|--------|--------------|
| `artist`      | String | Partition key |
| `album_title` | String | Sort key (`album#title`) |
| `album`       | String |              |
| `title`       | String |              |
| `year`        | String |              |
| `image_url`   | String |              |

### `subscribe`
Stores each user's subscribed songs.

| Attribute   | Type   | Key          |
|-------------|--------|--------------|
| `email`     | String | Partition key |
| `song_id`   | String | Sort key (`artist#album#title`, lowercased) |
| `artist`    | String |              |
| `title`     | String |              |
| `album`     | String |              |
| `year`      | String |              |
| `image_url` | String |              |

---

## Lambda Functions

### `login-function.py`
**Method:** POST

Validates email and password against the `login` table.

**Request body:**
```json
{
  "email": "user@example.com",
  "password": "secret"
}
```

**Responses:**
- `200` — Login success, returns `user_name`
- `401` — User not found or wrong password

---

### `register-function.py`
**Method:** POST

Creates a new user in the `login` table. Rejects duplicate emails.

**Request body:**
```json
{
  "email": "user@example.com",
  "user_name": "Yunie",
  "password": "secret"
}
```

**Responses:**
- `200` — User registered successfully
- `400` — Email already exists

---

### `query-function.py`
**Method:** GET

Searches the `music` table. Filters by any combination of `artist`, `album`, `year`, and `title`.

**Query parameters (all optional):**
| Parameter | Description                            |
|-----------|----------------------------------------|
| `artist`  | Exact match — uses DynamoDB Query      |
| `album`   | Exact match filter                     |
| `year`    | Exact match filter                     |
| `title`   | Case-insensitive substring match       |

If no `artist` is provided, a full table scan is performed.

**Response:** `200` with a JSON array of matching songs.

---

### `subscribe-function.py`
**Method:** GET or POST

**GET** — Returns all subscriptions for a user.

Query parameter: `email`

**POST** — Subscribes a user to a song. Validates the song exists in the `music` table before subscribing. Prevents duplicate subscriptions using a DynamoDB condition expression.

**POST request body:**
```json
{
  "email": "user@example.com",
  "artist": "The Beatles",
  "title": "Let It Be",
  "album": "Let It Be",
  "year": "1970",
  "image_url": "https://..."
}
```

**Responses:**
- `200` — Subscribed successfully / returns subscription list
- `400` — Already subscribed, invalid song, or missing fields
- `500` — Internal error

---

### `remove-function.py`
**Method:** POST (or DELETE via API Gateway mapping)

Removes a subscription from the `subscribe` table using `email` + `song_id` as the composite key.

**Request body:**
```json
{
  "email": "user@example.com",
  "artist": "The Beatles",
  "title": "Let It Be",
  "album": "Let It Be"
}
```

**Responses:**
- `200` — Removed successfully
- `400` — Missing required fields

---

## Notes

- All responses include `"Access-Control-Allow-Origin": "*"` for CORS compatibility with the React frontend.
- Lambda responses are wrapped objects — the frontend must parse `response.body` (a JSON string) explicitly.
- `Decimal` values from DynamoDB are converted to `int` before serialisation to avoid JSON errors.
- The `subscribe` table uses a composite sort key (`artist#album#title`, lowercased) to prevent duplicate entries per user.
- Passwords are stored in plaintext — suitable for a student project only. Production deployments should use hashing (e.g. bcrypt).

---

## Local Development

DynamoDB Local can be used for offline testing:

```bash
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```

See the [DynamoDB Local docs](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html) for full options.
