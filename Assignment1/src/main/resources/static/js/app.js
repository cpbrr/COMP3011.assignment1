const TRANSCRIBE_URL = "/api/v1/transcribe";

const startButton = document.getElementById("startButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("status");
const transcriptText = document.getElementById("transcript");
const errorText = document.getElementById("error");

let mediaRecorder = null;
let mediaStream = null;
let chunks = [];

function setStatus(message, recording) {
  statusText.textContent = message;
  statusText.classList.toggle("recording", recording === true);
}

function showError(message) {
  errorText.textContent = message;
  errorText.hidden = false;
}

function clearError() {
  errorText.textContent = "";
  errorText.hidden = true;
}

function releaseMicrophone() {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop());
    mediaStream = null;
  }
}

function readyForNextRecording() {
  startButton.disabled = false;
  stopButton.disabled = false;
  setStatus("Ready to record", false);
}

function describeMicrophoneError(error) {
  if (error.name === "NotAllowedError") {
    return "Microphone access was denied. Allow microphone permission and try again.";
  }
  if (error.name === "NotFoundError") {
    return "No microphone was found on this device.";
  }
  return `Could not start recording: ${error.message}`;
}

async function sendForTranscription(blob) {
  const formData = new FormData();
  formData.append("audio", blob, "recording.webm");

  const response = await fetch(TRANSCRIBE_URL, { method: "POST", body: formData });
  if (!response.ok) {
    throw new Error(`Transcription failed (HTTP ${response.status}).`);
  }

  const result = await response.json();
  return result.text ?? "";
}

async function handleStopped() {
  setStatus("Recording stopped. Transcribing...", false);
  releaseMicrophone();

  try {
    const blob = new Blob(chunks, { type: "audio/webm" });
    const text = await sendForTranscription(blob);
    transcriptText.textContent = text.trim() === "" ? "No speech was detected" : text;
  } catch (error) {
    showError(error.message);
  } finally {
    chunks = [];
    readyForNextRecording();
  }
}

async function startRecording() {
  clearError();
  startButton.disabled = true;
  setStatus("Requesting microphone access...", false);

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
  } catch (error) {
    showError(describeMicrophoneError(error));
    readyForNextRecording();
    return;
  }

  chunks = [];
  mediaRecorder = new MediaRecorder(mediaStream);

  mediaRecorder.addEventListener("dataavailable", (event) => {
    if (event.data.size > 0) {
      chunks.push(event.data);
    }
  });

  mediaRecorder.addEventListener("stop", handleStopped);

  mediaRecorder.start();
  setStatus("Recording in progress. Speak now.", true);
}

function stopRecording() {
  if (mediaRecorder && mediaRecorder.state === "recording") {
    mediaRecorder.stop();
  }
}

startButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

readyForNextRecording();
