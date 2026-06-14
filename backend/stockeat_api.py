import json
import os
import flask
from google import genai
from google.genai import types
from flask import jsonify
import requests

from dotenv import load_dotenv

load_dotenv()

app = flask.Flask(__name__)

API_KEY = os.environ.get('GEMINI_API_KEY')

if not API_KEY:
    raise ValueError("Falta la variable de entorno GEMINI_API_KEY")

OPEN_FOOD_FACTS_URL = "https://world.openfoodfacts.org/api/v2/product/"

client = genai.Client(api_key=API_KEY)

API_SECRET = os.environ.get('API_SECRET')

@app.before_request
def check_api_key():
    key = flask.request.headers.get('X-API-Key')
    if key != API_SECRET:
        return jsonify({"error": "No autorizado"}), 401
    return None


@app.route('/api/stockeat/generate-recipes', methods=['POST'])
def generate_recipes():
    data = flask.request.get_json()
    ingredients = data.get('ingredients', [])

    prompt = (
        f"Based ONLY on the following list of ingredients: {', '.join(ingredients)}, "
        f"suggest between 2 and 5 recipes that can be made with them.\n\n"

        f"STRICT RULES:\n"
        f"1. Prioritize and order the recipes based on the ingredients that may expire first (use perishable items first).\n"
        f"2. DO NOT include any extra ingredients outside the provided list, EXCEPT for basic pantry essentials if absolutely necessary (e.g., salt, black pepper, water, olive oil).\n"
        f"3. If you must suggest any other ingredient not present in the list, you MUST explicitly label it as '(optional)' inside the ingredients array.\n"
        f"4. It is not mandatory to use all provided ingredients in a single recipe.\n\n"

        f"OUTPUT FORMAT:\n"
        f"Return ONLY a raw JSON array of objects. Do not include markdown formatting like ```json or wrapping text. "
        f"Each object must follow this exact structure:\n"
        f"[{{\n"
        f"  'name': 'Recipe Name',\n"
        f"  'ingredients': ['ingredient 1', 'ingredient 2 (optional)'],\n"
        f"  'preparation': ['Step 1: clear description.', 'Step 2: clear description.']\n"
        f"}}]"
    )

    try:

        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
            config=types.GenerateContentConfig(
                system_instruction="Eres un chef experto en cocina de aprovechamiento. Tu objetivo es crear recetas ricas evitando el desperdicio de comida.",
                temperature=0.7,
                response_mime_type="application/json"
            ),
        )

        recipes_object = json.loads(response.text)

        return jsonify({
            "status": "success",
            "recipes": recipes_object
        }), 200

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500


@app.route('/api/stockeat/product/<barcode>', methods=['GET'])
def get_product(barcode):
    """
    Consulta la API de Open Food Facts para obtener información de un producto
    a partir de su código de barras.
    """
    # Validar un poco el código de barras
    if not barcode.isdigit():
        return jsonify({"error": "El código de barras debe contener solo números."}), 400

    try:
        # Hacer la petición a la API externa
        # Se añade un User-Agent como buena práctica y requisito de muchas APIs
        headers = {'User-Agent': 'MiAppFlask - Web - Versión 1.0'}
        response = requests.get(f"{OPEN_FOOD_FACTS_URL}{barcode}.json", headers=headers, timeout=5)

        # Si la API externa responde con un error de servidor (ej. 500 o 404 de red)
        response.raise_for_status()

        data = response.json()

        # Verifica si el producto realmente existe en la base de datos
        if data.get("status") == 0 or "product" not in data:
            return jsonify({"status": "no_encontrado", "mensaje": "Producto no registrado"}), 404

        # Filtra y estructura los datos
        producto = data["product"]
        resultado = {
            "status": "encontrado",
            "nombre": producto.get("product_name", "Producto desconocido"),
            "marca": producto.get("brands", "Genérico"),
            "categorias": producto.get("categories", ""),
            "imageUrl": producto.get("image_url", "")
        }
        print("--- DATOS ENVIADOS A ANDROID ---")
        print(f"Nombre: {resultado['nombre']}")
        print(f"URL de Imagen: {resultado['imageUrl']}")
        print("---------------------------------")

        return jsonify(resultado), 200

    except requests.exceptions.Timeout:
        return jsonify({"error": "La API externa tardó demasiado en responder."}), 504
    except requests.exceptions.RequestException as e:
        return jsonify({"error": "Error al conectar con el servicio de alimentos.", "detalles": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
