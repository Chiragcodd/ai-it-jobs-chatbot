let conversationHistory = [];
let isLoading = false;

async function sendMessage() {
  if (isLoading) return;
  const input = document.getElementById("userInput");
  const text = input.value.trim();
  if (!text) return;

  addMessage("user", text);
  conversationHistory.push({ role: "user", content: text });
  input.value = "";
  autoResize(input);

  const typingId = showTyping();
  setLoading(true);

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ messages: conversationHistory }),
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.error || "Server error");

    removeTyping(typingId);
    addMessage("bot", data.reply);
    conversationHistory.push({ role: "assistant", content: data.reply });
    if (conversationHistory.length > 20) conversationHistory = conversationHistory.slice(-20);
  } catch (err) {
    removeTyping(typingId);
    addMessage("bot", "❌ Error: " + err.message, true);
  } finally {
    setLoading(false);
  }
}

function sendQuick(text) {
  document.getElementById("userInput").value = text;
  sendMessage();
}

function addMessage(role, text, isError = false) {
  const messages = document.getElementById("messages");
  const div = document.createElement("div");
  div.className = `message ${role === "user" ? "user-msg" : "bot-msg"}`;

  const avatar = document.createElement("div");
  avatar.className = "msg-avatar";
  avatar.textContent = role === "user" ? "👤" : "🤖";

  const bubble = document.createElement("div");
  bubble.className = `msg-bubble${isError ? " error-bubble" : ""}`;
  bubble.innerHTML = formatText(text);

  div.appendChild(avatar);
  div.appendChild(bubble);
  messages.appendChild(div);
  scrollDown();
}

function formatText(text) {
  return text
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>")
    .replace(/`([^`]+)`/g, "<code>$1</code>")
    .replace(/^#{1,3} (.+)$/gm, "<h3>$1</h3>")
    .replace(/^[•\-\*] (.+)$/gm, "<li>$1</li>")
    .replace(/^\d+\. (.+)$/gm, "<li>$1</li>")
    .replace(/\n\n/g, "</p><p>")
    .replace(/\n/g, "<br>");
}

function showTyping() {
  const messages = document.getElementById("messages");
  const id = "typing-" + Date.now();
  const div = document.createElement("div");
  div.className = "message bot-msg";
  div.id = id;
  div.innerHTML = `<div class="msg-avatar">🤖</div><div class="msg-bubble"><div class="typing-dots"><div class="dot"></div><div class="dot"></div><div class="dot"></div></div></div>`;
  messages.appendChild(div);
  scrollDown();
  return id;
}

function removeTyping(id) {
  const el = document.getElementById(id);
  if (el) el.remove();
}

function setLoading(val) {
  isLoading = val;
  document.getElementById("sendBtn").disabled = val;
  document.getElementById("userInput").disabled = val;
}

function scrollDown() {
  const c = document.querySelector(".chat-container");
  setTimeout(() => { c.scrollTop = c.scrollHeight; }, 50);
}

function handleKey(e) {
  if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); sendMessage(); }
}

function autoResize(el) {
  el.style.height = "auto";
  el.style.height = Math.min(el.scrollHeight, 120) + "px";
}

window.addEventListener("load", () => { document.getElementById("userInput").focus(); });