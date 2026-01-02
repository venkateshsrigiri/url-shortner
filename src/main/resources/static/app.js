// src/main/resources/static/app.js

const urlInput = document.getElementById("urlInput");
const expiryInput = document.getElementById("expiryInput");
const shortenBtn = document.getElementById("shortenBtn");

const errorEl = document.getElementById("error");
const resultBox = document.getElementById("resultBox");
const shortUrlEl = document.getElementById("shortUrl");
const expiryTextEl = document.getElementById("expiryText");
const copyBtn = document.getElementById("copyBtn");

// Client-side URL validation (UX only; backend still validates)
function isValidHttpUrl(str) {
  try {
    const u = new URL(str);
    return u.protocol === "http:" || u.protocol === "https:";
  } catch {
    return false;
  }
}

function showError(msg) {
  errorEl.textContent = msg;
  errorEl.classList.remove("hidden");
}

function clearError() {
  errorEl.textContent = "";
  errorEl.classList.add("hidden");
}

function showResult(shortUrl, expiryAt) {
  resultBox.classList.remove("hidden");
  shortUrlEl.textContent = shortUrl;
  shortUrlEl.href = shortUrl;

  expiryTextEl.textContent = expiryAt
    ? "Expires at: " + new Date(expiryAt).toLocaleString()
    : "";
}

async function shorten() {
  clearError();
  resultBox.classList.add("hidden");

  const url = urlInput.value.trim();
  const expiry = expiryInput.value.trim();

  if (!url) {
    showError("Please enter a URL.");
    return;
  }

  if (!isValidHttpUrl(url)) {
    showError("Invalid URL. Only http/https URLs are allowed.");
    return;
  }

  let expiryMinutes = null;
  if (expiry) {
    const n = Number(expiry);
    if (!Number.isFinite(n) || n <= 0) {
      showError("Expiry must be a positive number (minutes).");
      return;
    }
    expiryMinutes = n;
  }

  try {
    const res = await fetch("/api/shorten", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url, expiryMinutes })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => null);
      showError(err?.message || "Failed to shorten URL.");
      return;
    }

    const data = await res.json();
    showResult(data.shortUrl, data.expiryAt);

  } catch {
    showError("Network error. Try again.");
  }
}

// ✅ Modern clipboard API only
async function copyToClipboard() {
  const text = shortUrlEl.textContent;
  if (!text) return;

  try {
    await navigator.clipboard.writeText(text);
    copyBtn.textContent = "Copied ✓";
    setTimeout(() => (copyBtn.textContent = "Copy"), 1200);
  } catch (err) {
    console.error("Clipboard copy failed:", err);
    showError("Copy failed. Please copy manually.");
  }
}

// Event listeners
shortenBtn.addEventListener("click", shorten);
copyBtn.addEventListener("click", copyToClipboard);

urlInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter") shorten();
});
