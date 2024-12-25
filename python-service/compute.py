from flask import Flask, request, jsonify
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.grpc.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.semconv.resource import ResourceAttributes

# Create a Resource with the service.name attribute 
resource = Resource.create({ResourceAttributes.SERVICE_NAME: "python-service"})

# Setup OpenTelemetry tracing
provider = TracerProvider(resource=resource)
processor = BatchSpanProcessor(OTLPSpanExporter(endpoint="http://otel-collector:4317", insecure=True))
provider.add_span_processor(processor)

# Sets the global default tracer provider
trace.set_tracer_provider(provider)

# Acquire a tracer 
tracer = trace.get_tracer("python-service")

app = Flask(__name__)

@app.route('/compute_average_age', methods=['POST'])
def compute_average_age():
    with tracer.start_as_current_span("compute_average_age"):
        data = request.json['data']
        if not data:
            return jsonify({'error': 'No data provided'}), 400
        ages = [item['age'] for item in data if 'age' in item]
        if not ages:
            return jsonify({'error': 'No age data available'}), 400
        average_age = round(sum(ages) / len(ages), 1)
        return jsonify({'average_age': average_age})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)