import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { API } from "../config";
import "../styles/MainPage.css";

export default function MainPage() {
  const navigate = useNavigate();

  const userEmail = sessionStorage.getItem("user_email");
  const userName = sessionStorage.getItem("user_name");

  const [subscriptions, setSubscriptions] = useState([]);
  const [queryResults, setQueryResults] = useState([]);
  const [noResults, setNoResults] = useState(false);

  const [titleInput, setTitleInput] = useState("");
  const [artistInput, setArtistInput] = useState("");
  const [yearInput, setYearInput] = useState("");
  const [albumInput, setAlbumInput] = useState("");

  // normalize API response
  const toArray = (data) => {
    if (!data) return [];

    // CASE 1: already array
    if (Array.isArray(data)) return data;

    // CASE 2: raw string (VERY IMPORTANT FIX)
    if (typeof data === "string") {
      try {
        const parsed = JSON.parse(data);
        return Array.isArray(parsed) ? parsed : [];
      } catch {
        return [];
      }
    }

    // CASE 3: API Gateway format
    if (data.body) {
      try {
        const parsed =
          typeof data.body === "string"
            ? JSON.parse(data.body)
            : data.body;

        return Array.isArray(parsed) ? parsed : [];
      } catch {
        return [];
      }
    }

    // CASE 4: songs wrapper
    if (data.songs && Array.isArray(data.songs)) return data.songs;

    return [];
  };

  // auth redirect
  useEffect(() => {
    if (!userEmail) navigate("/");
  }, [userEmail, navigate]);

  // load subscriptions
  useEffect(() => {
    const fetchSubscriptions = async () => {
      try {
        const res = await fetch(`${API.getSubscribe}?email=${userEmail}`);
        const raw = await res.json();
        setSubscriptions(Array.isArray(raw) ? raw : []);
      } catch {
        setSubscriptions([]);
      }
    };

    if (userEmail) fetchSubscriptions();
  }, [userEmail]);

  // load ALL songs on page load
  useEffect(() => {
    const fetchAllSongs = async () => {
      try {
        const res = await fetch(`${API.query}`);
        const data = await res.json();
        setQueryResults(toArray(data));
      } catch {
        setQueryResults([]);
      }
    };

    fetchAllSongs();
  }, []);

  const handleLogout = () => {
    sessionStorage.clear();
    navigate("/");
  };

  // SEARCH 
  const handleQuery = async () => {
    setNoResults(false);

    try {
      const params = new URLSearchParams();

      if (artistInput) params.append("artist", artistInput);
      if (albumInput) params.append("album", albumInput);
      if (yearInput) params.append("year", yearInput);
      if (titleInput) params.append("title", titleInput);

      const url =
        params.toString().length > 0
          ? `${API.query}?${params.toString()}`
          : `${API.query}`;

      const res = await fetch(url);
      const data = await res.json();

      const songs = toArray(data);

      setQueryResults(songs);
      setNoResults(songs.length === 0);
    } catch {
      setQueryResults([]);
    }
  };

  const handleSubscribe = async (song) => {
    try {
      const res = await fetch(API.subscribe, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: userEmail,
          artist: song.artist,
          title: song.title,
          album: song.album,
          year: song.year,
          image_url: song.image_url,
        }),
      });

      if (res.ok) {
        setSubscriptions((prev) => [...prev, song]);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleRemove = async (song) => {
    try {
      const params = new URLSearchParams({
        email: userEmail,
        song_id: song.song_id,
      });

      const res = await fetch(`${API.remove}?${params.toString()}`, {
        method: "DELETE",
      });

      if (res.ok) {
        // Refetch subscriptions from backend to ensure state sync
        const subsRes = await fetch(`${API.getSubscribe}?email=${userEmail}`);
        const rawSubs = await subsRes.json();
        //let parsed;
        setSubscriptions(Array.isArray(rawSubs) ? rawSubs : []);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const getTitle = (song) => {
    if (song.album_title) {
      return song.album_title.split("#")[1];
    }
    return song.title;
  };

  // UI styles 
  const cardStyle = {
    width: "320px",
    display: "flex",
    alignItems: "center",
    gap: "10px",
    padding: "10px",
    border: "1px solid #ddd",
    borderRadius: "10px",
  };

  const imgStyle = {
    width: "50px",
    height: "50px",
    objectFit: "cover",
    borderRadius: "6px",
  };

  const textStyle = { flex: 1 };

  const btnBase = {
    padding: "5px 10px",
    fontSize: "12px",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    color: "red",

  };

  return (
    <div className="main-page">
      <div className="main-header">
        <h1>Tune Vault</h1>
        <div className="main-user-info">
          <span>Welcome, {userName}</span>
          <button onClick={handleLogout} className="logout-btn">Logout</button>
        </div>
      </div>

      {/* SUBSCRIPTIONS */}
      <div className="section">
        <h2>Your Subscriptions</h2>
        <p>{subscriptions.length} subscription(s)</p>

        {subscriptions.length === 0 ? (
          <p>No subscriptions yet</p>
        ) : (
          <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
            {Array.isArray(subscriptions) && subscriptions.map((song, i) => (
              <div key={i} style={cardStyle}>
                <img src={song.image_url} alt="song" style={imgStyle} />
                <div style={textStyle}>
                  <h3 style={{ fontSize: "14px", margin: 0 }}>
                    {getTitle(song)}
                  </h3>
                  <p style={{ fontSize: "12px", margin: 0 }}>
                    {song.artist} - {song.album} ({song.year})
                  </p>
                </div>

                <button
                  onClick={() => handleRemove(song)}
                  style={{ ...btnBase, background: "#e58034" }}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* SEARCH */}
      <div className="section">
        <h2>Search Tunes</h2>
        <p>{queryResults.length} song(s) found</p>

        <div className="search-form">
          <input placeholder="Title" value={titleInput} onChange={(e) => setTitleInput(e.target.value)} />
          <input placeholder="Artist" value={artistInput} onChange={(e) => setArtistInput(e.target.value)} />
          <input placeholder="Year" value={yearInput} onChange={(e) => setYearInput(e.target.value)} />
          <input placeholder="Album" value={albumInput} onChange={(e) => setAlbumInput(e.target.value)} />
          <button
            onClick={handleQuery}
            style={{
              background: "orange",
              color: "red",
              border: "none",
              padding: "10px 18px",
              borderRadius: "10px",
              cursor: "pointer",
            }}
          >
            Search
          </button>
        </div>

        {noResults && <p>No results found</p>}

        <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
          {queryResults.map((song, i) => (
            <div key={i} style={cardStyle}>
              <img src={song.image_url} alt="song" style={imgStyle} />

              <div style={textStyle}>
                <h3 style={{ fontSize: "14px", margin: 0 }}>
                  {song.title}
                </h3>
                <p style={{ fontSize: "12px", margin: 0 }}>
                  {song.artist} - {song.album} ({song.year})
                </p>
              </div>

              <button
                onClick={() => handleSubscribe(song)}
                style={{ ...btnBase, background: "orange" }}
              >
                Subscribe
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}