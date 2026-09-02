#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D NormalSampler;

varying vec2 texCoord;
uniform vec2 InSize;
uniform float Time;
uniform float SSAORadius;
uniform float SSAOIntensity;
uniform float SSAOBias;

const int SAMPLE_COUNT = 16;

vec3 rand3(vec2 co) {
    vec3 r = fract(sin(vec3(dot(co, vec2(127.1, 311.7)), dot(co, vec2(269.5, 183.3)), dot(co, vec2(419.2, 371.9)))) * 43758.5453);
    return r * 2.0 - 1.0;
}

vec2 rand2(vec2 co) {
    return fract(sin(vec2(dot(co, vec2(127.1, 311.7)), dot(co, vec2(269.5, 183.3)))) * 43758.5453) * 2.0 - 1.0;
}

vec3 reconstructPosition(vec2 uv, float depth) {
    // Simple reconstruction - in reality would use inverse projection matrix
    float z = depth * 2.0 - 1.0;
    vec4 clip = vec4(uv * 2.0 - 1.0, z, 1.0);
    // Simplified - just use depth as view space Z
    return vec3(uv * 2.0 - 1.0, depth * 100.0);
}

vec3 reconstructNormal(vec2 uv) {
    // Sample normal texture or compute from depth
    vec3 normal = texture2D(NormalSampler, uv).rgb * 2.0 - 1.0;
    return normalize(normal);
}

float getDepth(vec2 uv) {
    return texture2D(DepthSampler, uv).r;
}

void main() {
    vec2 uv = texCoord;
    vec4 color = texture2D(DiffuseSampler, uv);
    
    float centerDepth = getDepth(uv);
    if (centerDepth >= 1.0) {
        gl_FragColor = color;
        return;
    }
    
    vec3 centerPos = reconstructPosition(uv, centerDepth);
    vec3 centerNormal = reconstructNormal(uv);
    
    // Generate sample kernel
    vec2 noiseScale = InSize / 4.0;
    vec2 randVec = rand2(uv * noiseScale);
    
    float occlusion = 0.0;
    int samples = 0;
    
    // Hemisphere sampling
    for (int i = 0; i < SAMPLE_COUNT; i++) {
        // Generate sample direction using spherical coordinates
        float angle = float(i) * 6.28318 / float(SAMPLE_COUNT);
        float radius = SSAORadius * (0.5 + 0.5 * float(i) / float(SAMPLE_COUNT));
        
        vec2 sampleDir = vec2(cos(angle), sin(angle)) * radius;
        vec2 sampleUV = uv + sampleDir / InSize;
        
        // Add noise for dithering
        sampleUV += randVec * 0.01;
        
        float sampleDepth = getDepth(sampleUV);
        if (sampleDepth >= 1.0) continue;
        
        vec3 samplePos = reconstructPosition(sampleUV, sampleDepth);
        vec3 toSample = samplePos - centerPos;
        float dist = length(toSample);
        
        if (dist < SSAORadius) {
            float rangeCheck = smoothstep(0.0, SSAORadius, dist);
            float depthDiff = sampleDepth - centerDepth;
            float ao = step(SSAOBias, depthDiff) * (1.0 - rangeCheck);
            occlusion += ao;
        }
        samples++;
    }
    
    if (samples > 0) {
        occlusion = 1.0 - (occlusion / float(samples)) * SSAOIntensity;
        occlusion = clamp(occlusion, 0.0, 1.0);
        
        // Blur occlusion (simple 3x3)
        float blurOcclusion = 0.0;
        float blurWeight = 0.0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                vec2 offset = vec2(float(x), float(y)) / InSize;
                float w = 1.0 - length(vec2(float(x), float(y))) / 1.5;
                blurOcclusion += texture2D(DepthSampler, uv + offset).r * w;
                blurWeight += w;
            }
        }
        if (blurWeight > 0.0) {
            occlusion = mix(occlusion, blurOcclusion / blurWeight, 0.5);
        }
    }
    
    vec3 finalColor = color.rgb * occlusion;
    gl_FragColor = vec4(finalColor, color.a);
}