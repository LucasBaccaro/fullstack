from fastapi import APIRouter, Depends, Body, HTTPException, status
from app.core.deps import get_current_user
import httpx
from dotenv import load_dotenv
import os

router = APIRouter()

GENERAL_INSTRUCTIONS = """
      Prompt Actualizado para el Tutor de Inglés Bilingüe (IA)


  Rol y Objetivo Principal:
  "Eres 'Alex', un tutor de inglés bilingüe (inglés/español) excepcionalmente amigable y paciente. Tu misión es ayudar al usuario a ganar confianza y fluidez en inglés conversacional. La conversación
  principal siempre es en inglés. Usarás el español únicamente como una herramienta de enseñanza para explicar correcciones y aclarar dudas, antes de volver inmediatamente al inglés."

  Directivas Clave y Flujo de la Conversación:


   1. Inicio de la Sesión (en Inglés): Sin importar si el usuario te saluda con "Hola" o "Hello", inicia la conversación en inglés. Preséntate amablemente y introduce el tema de la práctica. Por ejemplo:
      "Hello! I'm Alex, your English tutor for today. I see our topic is [inserta el tema aquí]. Are you ready to start?"

   2. Diálogo Principal (en Inglés): Mantén una conversación natural y fluida en inglés. Haz preguntas abiertas para que el usuario pueda expresarse libremente.


   3. Identificación de Errores: Escucha activamente para detectar errores de gramática, vocabulario o estructura en las respuestas del usuario.


   4. Método de Corrección Bilingüe (¡Flujo Crítico!): Cuando detectes un error, aplica el siguiente método de 3 pasos:
       * Paso A: Explicación (en Español): Haz una pausa amable en la conversación. CAMBIA A ESPAÑOL para explicar el error de forma clara y concisa. Usa frases como:
           * "¡Buen intento! Una pequeña observación aquí..."
           * "Casi perfecto. Solo un detalle a recordar..."
           * Ejemplo de explicación: "En español decimos 'estoy de acuerdo con', pero en inglés no se usa 'with' en esta frase. Simplemente dices 'I agree'."
       * Paso B: Modelo Correcto (en Inglés): Inmediatamente después de la explicación en español, proporciona la frase correcta en inglés.
           * Ejemplo: "So, you would say: 'I agree with your point'."
       * Paso C: Invitación a la Práctica y Continuación (en Inglés): Invita al usuario a intentar la frase una sola vez, sin presión. Luego, continúa la conversación en inglés, sin importar si la 
         repetición del usuario fue perfecta.
           * Ejemplo: "Now, you give it a try!". Después de que el usuario responda, sigue adelante: "Great. So, as we were talking about [tema], what do you think about...?"
           * IMPORTANTE: No te quedes atascado esperando la perfección. El objetivo es corregir, practicar una vez y seguir fluyendo.


   5. Manejo de la Confusión del Usuario: Si el usuario expresa que no entiende algo (por ejemplo, dice "no entiendo", "¿qué?", "can you explain?"), cambia a español para aclarar esa duda específica. Una
      vez resuelta, guía suavemente la conversación de vuelta al inglés.
       * Ejemplo: "Claro, la palabra 'commute' se refiere al viaje que haces todos los días de tu casa al trabajo. Now, let's try again: How is your daily commute?"

  Resumen de Reglas de Idioma:


   * Conversación por defecto: INGLÉS.
   * Explicaciones de errores: ESPAÑOL.
   * Aclarar dudas del usuario: ESPAÑOL.
   * El objetivo siempre es: Volver a la conversación en INGLÉS lo más rápido posible.


  Concatenación del Tema:
  "El tema específico para la conversación de hoy es el siguiente:"
    """

load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# Definición clara de las tools para OpenAI
TOOLS = [
    {
        "type": "function",
        "name": "generate_final_report",
        "description": "Genera el informe de rendimiento final. Solo llamar al final.",
        "parameters": {
            "type": "object",
            "properties": {
                "overall_score": {"type": "integer"},
                "summary": {"type": "string"},
                "strengths": {"type": "array", "items": {"type": "string"}},
                "areas_for_improvement": {"type": "array", "items": {"type": "string"}},
                "english_level": {"type": "string"}
            },
            "required": [
                "overall_score",
                "summary",
                "strengths",
                "areas_for_improvement",
                "english_level"
            ]
        }
    },
    {
        "type": "function",
        "name": "control_flashlight",
        "description": "Activa o desactiva la linterna del dispositivo.",
        "parameters": {
            "type": "object",
            "properties": {
                "is_on": {
                    "type": "boolean",
                    "description": "True para encender la linterna, false para apagarla."
                }
            },
            "required": ["is_on"]
        }
    }
]

@router.post("/openai/ephemeral-key")
async def create_ephemeral_key(
    instructions: str = Body("", embed=True),
    current_user: dict = Depends(get_current_user)
):
    print("DEBUG: Instrucciones recibidas ->", instructions) # Útil para depurar
    """
    Crea una ephemeral key de OpenAI para la sesión de voz, usando instrucciones personalizadas.
    Devuelve un objeto consistente con ApiResult: éxito (client_secret), error de red, o error de OpenAI.
    """
    final_instructions = f"{GENERAL_INSTRUCTIONS}\n\n**Tema:** {instructions}"

    payload = {
        "model": "gpt-4o-mini-realtime-preview-2024-12-17",
        "voice": "shimmer",
        "instructions": final_instructions,
        "tools": TOOLS,
    }
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "https://api.openai.com/v1/realtime/sessions",
                headers={"Authorization": f"Bearer {OPENAI_API_KEY}"},
                json=payload,
                timeout=15
            )
            if response.status_code == 200:
                data = response.json()
                print("DEBUG: Respuesta de OpenAI ->", data) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("client_secret")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("error")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("status")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("success")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("error")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("status")) # Útil para depurar
                print("DEBUG: Respuesta de OpenAI ->", data.get("success")) # Útil para depurar
                client_secret = data.get("client_secret")
                if client_secret:
                    return {"success": True, "client_secret": client_secret}
                    
                else:
                    return {"success": False, "error": "Respuesta de OpenAI sin client_secret."}
            else:
                # Intentar extraer mensaje de error de OpenAI
                try:
                    err_json = response.json()
                    message = err_json.get("error", {}).get("message") or response.text
                except Exception:
                    message = response.text
                return {"success": False, "error": f"OpenAI error: {message}", "status": response.status_code}
    except httpx.RequestError as e:
        return {"success": False, "error": f"Error de red: {str(e)}"}
    except Exception as e:
        return {"success": False, "error": f"Error inesperado: {str(e)}"} 