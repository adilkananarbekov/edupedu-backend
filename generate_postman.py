import json
import uuid

def resolve_schema(schema, components):
    if "$ref" in schema:
        ref_path = schema["$ref"].split("/")
        return components["schemas"][ref_path[-1]]
    return schema

def generate_sample_json(schema, components, visited=None):
    if visited is None:
        visited = set()
    
    schema = resolve_schema(schema, components)
    
    # Simple recursion protection
    schema_id = id(json.dumps(schema, sort_keys=True))
    if schema_id in visited:
        return {}
    visited.add(schema_id)

    stype = schema.get("type")
    
    if stype == "object":
        obj = {}
        properties = schema.get("properties", {})
        for prop_name, prop_schema in properties.items():
            obj[prop_name] = generate_sample_json(prop_schema, components, visited.copy())
        return obj
    elif stype == "array":
        items_schema = schema.get("items", {})
        return [generate_sample_json(items_schema, components, visited.copy())]
    elif stype == "string":
        if "enum" in schema:
            return schema["enum"][0]
        if schema.get("format") == "date-time":
            return "2023-10-27T10:00:00Z"
        if schema.get("format") == "email":
            return "user@example.com"
        return "string"
    elif stype == "integer":
        return 0
    elif stype == "number":
        return 0.0
    elif stype == "boolean":
        return True
    
    return None

def generate_postman_collection(openapi_path, output_path):
    with open(openapi_path, 'r') as f:
        openapi = json.load(f)
    
    components = openapi.get("components", {})
    collection = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": openapi.get("info", {}).get("title", "EduPedu API"),
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": [],
        "auth": {
            "type": "bearer",
            "bearer": [
                {
                    "key": "token",
                    "value": "{{accessToken}}",
                    "type": "string"
                }
            ]
        }
    }
    
    folders = {}
    
    paths = openapi.get("paths", {})
    for path, methods in paths.items():
        for method, details in methods.items():
            tag = details.get("tags", ["Default"])[0]
            if tag not in folders:
                folders[tag] = {
                    "name": tag,
                    "item": []
                }
            
            # Postman request object
            pm_request = {
                "name": details.get("operationId", f"{method.upper()} {path}"),
                "request": {
                    "method": method.upper(),
                    "header": [],
                    "url": {
                        "raw": "{{baseUrl}}" + path.replace("{", ":").replace("}", ""),
                        "host": ["{{baseUrl}}"],
                        "path": [p for p in path.strip("/").split("/") if p],
                        "variable": []
                    }
                },
                "response": []
            }
            
            # Path variables
            for param in details.get("parameters", []):
                if param.get("in") == "path":
                    pm_request["request"]["url"]["variable"].append({
                        "key": param["name"],
                        "value": "1" # Default ID
                    })
                elif param.get("in") == "query":
                    if "query" not in pm_request["request"]["url"]:
                        pm_request["request"]["url"]["query"] = []
                    pm_request["request"]["url"]["query"].append({
                        "key": param["name"],
                        "value": ""
                    })

            # Body Generation
            if "requestBody" in details:
                content = details["requestBody"].get("content", {})
                if "application/json" in content:
                    schema = content["application/json"].get("schema", {})
                    sample_body = generate_sample_json(schema, components)
                    pm_request["request"]["body"] = {
                        "mode": "raw",
                        "raw": json.dumps(sample_body, indent=2),
                        "options": {
                            "raw": {
                                "language": "json"
                            }
                        }
                    }

            folders[tag]["item"].append(pm_request)
            
    collection["item"] = list(folders.values())
    
    with open(output_path, 'w') as f:
        json.dump(collection, f, indent=2)

def generate_postman_environment(output_path):
    env = {
        "id": str(uuid.uuid4()),
        "name": "EduPedu Server",
        "values": [
            {
                "key": "baseUrl",
                "value": "http://136.116.64.6:8080",
                "enabled": True
            },
            {
                "key": "accessToken",
                "value": "",
                "enabled": True
            }
        ],
        "_postman_variable_scope": "environment"
    }
    with open(output_path, 'w') as f:
        json.dump(env, f, indent=2)

if __name__ == "__main__":
    generate_postman_collection("openapi.json", "edupedu_collection.json")
    generate_postman_environment("edupedu_environment.json")
    print("Postman collection and environment generated with sample JSON bodies.")
