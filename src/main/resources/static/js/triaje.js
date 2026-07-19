document.addEventListener("DOMContentLoaded", () => {
  const token = sessionStorage.getItem("token");
  const chatMensajes = document.getElementById("chatMensajes");
  const inputMensaje = document.getElementById("mensajeTriaje");
  const botonEnviar = document.getElementById("botonEnviarTriaje");
  const botonVoz = document.getElementById("botonVozTriaje");

  if (!chatMensajes || !inputMensaje || !botonEnviar || !botonVoz) {
    return;
  }

  const historial = [];
  let mediaRecorder = null;
  let audioChunks = [];
  let grabando = false;
  let ultimaRespuestaIA = "";
  let botonVozActivo = null;

  chatMensajes.innerHTML = "";
  agregarMensajeIA(
    "Hola. Soy el asistente de triaje psicologico de MindU. Cuéntame como te has sentido en las ultimas 48 horas.",
  );

  botonEnviar.addEventListener("click", enviarMensaje);
  inputMensaje.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      enviarMensaje();
    }
  });
  botonVoz.addEventListener("click", alternarGrabacion);

  async function enviarMensaje() {
    const mensaje = inputMensaje.value.trim();
    if (!mensaje) {
      return;
    }

    inputMensaje.value = "";
    agregarMensajeUsuario(mensaje);
    historial.push({ rol: "user", contenido: mensaje });
    mostrarEscribiendo();
    bloquearEntrada(true);

    try {
      const response = await fetch("/api/triaje/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          mensaje,
          historial: historial.slice(-10),
        }),
      });

      if (!response.ok) {
        throw new Error("No se pudo obtener respuesta del servidor");
      }

      const data = await response.json();
      quitarEscribiendo();
      const respuesta =
        data.respuesta || "No pude generar una respuesta en este momento.";
      ultimaRespuestaIA = respuesta;
      agregarMensajeIA(respuesta);
      if (data.sugerirCita) {
        mostrarBannerCita();
      }
      historial.push({ rol: "assistant", contenido: respuesta });
    } catch (error) {
      quitarEscribiendo();
      agregarMensajeIA(
        "Tu mensaje llego al chat, pero hubo un problema al consultar la IA. Revisa el backend o la clave de OpenAI.",
      );
      console.error(error);
    } finally {
      bloquearEntrada(false);
      inputMensaje.focus();
    }
  }

  async function alternarGrabacion() {
    if (grabando) {
      detenerGrabacion();
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioChunks = [];
      const mimeType = obtenerMimeType();
      mediaRecorder = mimeType
        ? new MediaRecorder(stream, { mimeType })
        : new MediaRecorder(stream);

      mediaRecorder.addEventListener("dataavailable", (event) => {
        if (event.data.size > 0) {
          audioChunks.push(event.data);
        }
      });

      mediaRecorder.addEventListener("stop", async () => {
        stream.getTracks().forEach((track) => track.stop());
        await transcribirAudio();
      });

      mediaRecorder.start();
      grabando = true;
      botonVoz.classList.add("bg-red-50", "text-red-600");
      botonVoz.querySelector(".material-symbols-outlined").textContent = "stop";
    } catch (error) {
      agregarMensajeIA(
        "No pude acceder al microfono. Revisa los permisos del navegador.",
      );
      console.error(error);
    }
  }

  function detenerGrabacion() {
    if (mediaRecorder && mediaRecorder.state !== "inactive") {
      mediaRecorder.stop();
    }
    grabando = false;
    botonVoz.classList.remove("bg-red-50", "text-red-600");
    botonVoz.querySelector(".material-symbols-outlined").textContent = "mic";
  }

  async function transcribirAudio() {
    if (!audioChunks.length) {
      return;
    }

    const audioBlob = new Blob(audioChunks, { type: obtenerMimeType() });
    const formData = new FormData();
    formData.append("audio", audioBlob, "triaje-voz.webm");
    bloquearEntrada(true);
    inputMensaje.placeholder = "Transcribiendo audio...";

    try {
      const response = await fetch("/api/triaje/transcribir", {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        throw new Error("No se pudo transcribir el audio");
      }

      const data = await response.json();
      inputMensaje.value = data.texto || "";
      if (inputMensaje.value.trim()) {
        await enviarMensaje();
      }
    } catch (error) {
      agregarMensajeIA(
        "No pude transcribir el audio. Intenta grabar de nuevo o escribe tu mensaje.",
      );
      console.error(error);
    } finally {
      inputMensaje.placeholder = "Escribe tu respuesta...";
      bloquearEntrada(false);
    }
  }

  function obtenerMimeType() {
    if (MediaRecorder.isTypeSupported("audio/webm;codecs=opus")) {
      return "audio/webm;codecs=opus";
    }
    if (MediaRecorder.isTypeSupported("audio/webm")) {
      return "audio/webm";
    }
    return "";
  }

  function agregarMensajeUsuario(texto) {
    const wrapper = document.createElement("div");
    wrapper.className = "flex gap-4 max-w-3xl ml-auto justify-end";
    wrapper.innerHTML = `
            <div class="bg-primary text-white p-4 rounded-xl text-body-sm shadow-sm leading-relaxed max-w-2xl"></div>
            <div class="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-[18px]">person</span>
            </div>
        `;
    wrapper.querySelector("div").textContent = texto;
    chatMensajes.appendChild(wrapper);
    scrollAlFinal();
  }

  function agregarMensajeIA(texto) {
    const wrapper = document.createElement("div");
    wrapper.className = "flex gap-4 max-w-3xl";
    wrapper.innerHTML = `
        <div class="w-8 h-8 bg-surface border border-surface-container rounded-full flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-secondary text-[18px]">smart_toy</span>
        </div>

        <div>
            <div class="bg-white p-4 rounded-xl border border-surface-container text-body-sm text-primary shadow-sm leading-relaxed"></div>

            <button class="botonVozRespuesta mt-2 text-sm text-primary">
                🔊 Escuchar respuesta
            </button>
        </div>
    `;
    wrapper.querySelector(".bg-white").textContent = texto;

    const botonVoz = wrapper.querySelector(".botonVozRespuesta");

    botonVoz.addEventListener("click", () => {
      alternarLectura(texto, botonVoz);
    });

    chatMensajes.appendChild(wrapper);
    scrollAlFinal();
  }

  function mostrarEscribiendo() {
    const typing = document.createElement("div");
    typing.id = "triajeTyping";
    typing.className = "flex gap-4 max-w-3xl";
    typing.innerHTML = `
            <div class="w-8 h-8 bg-surface border border-surface-container rounded-full flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-secondary text-[18px]">smart_toy</span>
            </div>
            <div class="bg-white p-4 rounded-xl border border-surface-container text-body-sm text-secondary shadow-sm leading-relaxed">
                Analizando...
            </div>
        `;
    chatMensajes.appendChild(typing);
    scrollAlFinal();
  }

  function quitarEscribiendo() {
    const typing = document.getElementById("triajeTyping");
    if (typing) {
      typing.remove();
    }
  }

  function bloquearEntrada(bloquear) {
    inputMensaje.disabled = bloquear;
    botonEnviar.disabled = bloquear;
    botonEnviar.classList.toggle("opacity-50", bloquear);
  }

  function scrollAlFinal() {
    chatMensajes.scrollTop = chatMensajes.scrollHeight;
  }

function mostrarBannerCita() {
    const banner = document.createElement("div");
    banner.className = "flex gap-4 max-w-3xl";
    banner.innerHTML = `
            <div class="w-8 h-8 bg-error/10 rounded-full flex items-center justify-center flex-shrink-0">
                <span class="material-symbols-outlined text-error text-[18px]">favorite</span>
            </div>
            <div class="bg-red-50 border border-red-200 p-4 rounded-xl text-body-sm text-zinc-800 shadow-sm leading-relaxed">
                <p class="mb-3">Notamos que esto podría ser importante. Te recomendamos hablar con un psicólogo de MindU lo antes posible.</p>
                <a href="citas.html" class="inline-block bg-error text-white text-xs font-bold uppercase tracking-wider px-4 py-2 rounded-lg">Agendar una cita</a>
            </div>
        `;
    chatMensajes.appendChild(banner);
    scrollAlFinal();
  }

  function alternarLectura(texto, boton) {
    if (!("speechSynthesis" in window)) {
      alert("Tu navegador no soporta lectura de voz");
      return;
    }

    const estabaSonandoEsteBoton =
      botonVozActivo === boton && window.speechSynthesis.speaking;

    window.speechSynthesis.cancel();
    if (botonVozActivo) {
      botonVozActivo.textContent = "🔊 Escuchar respuesta";
    }

    if (estabaSonandoEsteBoton) {
      botonVozActivo = null;
      return;
    }

    const voz = new SpeechSynthesisUtterance(texto);
    voz.lang = "es-ES";
    voz.rate = 1;
    voz.pitch = 1;
    voz.onend = () => {
      boton.textContent = "🔊 Escuchar respuesta";
      botonVozActivo = null;
    };

    boton.textContent = "⏸️ Detener";
    botonVozActivo = boton;
    window.speechSynthesis.speak(voz);
  }
});
