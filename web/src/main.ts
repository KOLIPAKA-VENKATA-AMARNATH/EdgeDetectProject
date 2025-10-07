const imgEl = document.getElementById('frame') as HTMLImageElement;
const fpsEl = document.getElementById('fps') as HTMLSpanElement;
const resEl = document.getElementById('res') as HTMLSpanElement;

// Dummy base64 image placeholder (transparent PNG)
const sampleBase64 =
  'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAQCAYAAABWc8w1AAAAIklEQVR42u3BAQ0AAADCIPunNscwAAAAAAAAAAAAAAAAAAD4CwV7AAGr2r3OAAAAAElFTkSuQmCC';

let frameCount: number = 0;
let last: number = performance.now();

function updateStats(width: number, height: number) {
  frameCount++;
  const now = performance.now();
  if (now - last > 1000) {
    fpsEl.textContent = String(frameCount);
    frameCount = 0;
    last = now;
  }
  resEl.textContent = `${width}x${height}`;
}

function loadSample() {
  imgEl.src = sampleBase64;
  imgEl.onload = () => {
    updateStats(imgEl.naturalWidth, imgEl.naturalHeight);
  };
}

// Simulate receiving processed frame
loadSample();
