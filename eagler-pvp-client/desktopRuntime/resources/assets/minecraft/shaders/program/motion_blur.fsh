#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D VelocitySampler;

varying vec2 texCoord;
uniform vec2 InSize;
uniform float Time;
uniform float MotionBlurStrength;
uniform int SampleCount;

void main() {
    vec2 uv = texCoord;
    vec4 color = texture2D(DiffuseSampler, uv);
    
    // Sample velocity buffer
    vec2 velocity = texture2D(VelocitySampler, uv).rg * 2.0 - 1.0;
    
    // Scale velocity by strength
    velocity *= MotionBlurStrength;
    
    // Accumulate color along velocity vector
    vec3 accumulatedColor = color.rgb;
    float weightSum = 1.0;
    
    int samples = SampleCount;
    for (int i = 1; i <= samples; i++) {
        float t = float(i) / float(samples);
        vec2 sampleUV = uv - velocity * t;
        
        // Bounds check
        if (sampleUV.x >= 0.0 && sampleUV.x <= 1.0 && sampleUV.y >= 0.0 && sampleUV.y <= 1.0) {
            vec4 sampleColor = texture2D(DiffuseSampler, sampleUV);
            float weight = 1.0 - t * 0.5; // Fade towards tail
            accumulatedColor += sampleColor.rgb * weight;
            weightSum += weight;
        }
    }
    
    vec3 finalColor = accumulatedColor / weightSum;
    
    gl_FragColor = vec4(finalColor, color.a);
}