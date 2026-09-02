#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
uniform vec2 InSize;
uniform float Time;
uniform float Contrast;
uniform float Saturation;
uniform float Brightness;
uniform vec3 ColorFilter;
uniform float Temperature;
uniform float Tint;

void main() {
    vec2 uv = texCoord;
    vec4 color = texture2D(DiffuseSampler, uv);
    vec3 c = color.rgb;
    
    // Temperature (warmth/coolness)
    if (Temperature != 0.0) {
        float t = Temperature * 0.1;
        c.r *= 1.0 + t;
        c.b *= 1.0 - t;
    }
    
    // Tint (green/magenta)
    if (Tint != 0.0) {
        float tn = Tint * 0.1;
        c.g *= 1.0 + tn;
        c.r *= 1.0 - tn * 0.5;
        c.b *= 1.0 - tn * 0.5;
    }
    
    // Contrast
    if (Contrast != 0.0) {
        c = (c - 0.5) * (1.0 + Contrast) + 0.5;
    }
    
    // Brightness
    if (Brightness != 0.0) {
        c += vec3(Brightness);
    }
    
    // Saturation
    if (Saturation != 0.0) {
        float luminance = dot(c, vec3(0.2126, 0.7152, 0.0722));
        vec3 gray = vec3(luminance);
        c = mix(gray, c, 1.0 + Saturation);
    }
    
    // Color filter (creative LUT-style)
    c *= ColorFilter;
    
    // Vignette
    float vignetteStrength = 0.5;
    vec2 center = uv - 0.5;
    float dist = length(center) * 1.4;
    float vignette = smoothstep(0.5, 1.0, dist);
    vignette = 1.0 - vignette * vignetteStrength;
    c *= vignette;
    
    // Film grain
    float grainAmount = 0.02;
    float noise = fract(sin(dot(uv * InSize + Time * 100.0, vec2(12.9898, 78.233))) * 43758.5453);
    c += (noise - 0.5) * grainAmount;
    
    // Clamp
    c = clamp(c, 0.0, 1.0);
    
    // Tone mapping (ACES filmic)
    c = c * (2.51 * c + 0.03) / (c * (2.43 * c + 0.59) + 0.14);
    
    // Gamma correction
    c = pow(c, vec3(1.0/2.2));
    
    gl_FragColor = vec4(c, color.a);
}