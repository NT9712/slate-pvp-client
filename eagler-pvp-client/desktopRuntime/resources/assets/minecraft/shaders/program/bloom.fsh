#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

varying vec2 texCoord;
uniform vec2 InSize;
uniform float Time;
uniform float BloomIntensity;
uniform float BloomThreshold;

void main() {
    vec2 uv = texCoord;
    vec4 color = texture2D(DiffuseSampler, uv);
    float depth = texture2D(DepthSampler, uv).r;
    
    // Bloom threshold - extract bright areas
    float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 bloomColor = vec3(0.0);
    
    if (brightness > BloomThreshold) {
        bloomColor = color.rgb * ((brightness - BloomThreshold) / (1.0 - BloomThreshold)) * BloomIntensity;
    }
    
    // Simple Gaussian blur approximation for bloom
    vec2 texelSize = 1.0 / InSize;
    vec3 blur = vec3(0.0);
    float weightSum = 0.0;
    
    // 5x5 blur kernel
    for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
            vec2 offset = vec2(float(x), float(y)) * texelSize * 2.0;
            float weight = exp(-(float(x*x + y*y)) / 2.0);
            vec4 sample = texture2D(DiffuseSampler, uv + offset);
            float sampleBrightness = dot(sample.rgb, vec3(0.2126, 0.7152, 0.0722));
            if (sampleBrightness > BloomThreshold) {
                blur += sample.rgb * weight;
                weightSum += weight;
            }
        }
    }
    
    if (weightSum > 0.0) {
        blur /= weightSum;
    }
    
    // Combine original + bloom
    vec3 finalColor = color.rgb + blur * BloomIntensity;
    
    // Tone mapping (simple Reinhard)
    finalColor = finalColor / (finalColor + vec3(1.0));
    
    // Gamma correction
    finalColor = pow(finalColor, vec3(1.0/2.2));
    
    gl_FragColor = vec4(finalColor, color.a);
}