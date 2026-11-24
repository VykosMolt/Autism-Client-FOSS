#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
layout(location = 0) in vec2 markerUv;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) flat in int markerStyle;
layout(location = 0) out vec4 fragColor;

float stroke(float d, float width, float aa) {
    return 1.0 - smoothstep(width - aa * 0.5, width + aa * 0.5, d);
}
void main() {
    vec2 p = markerUv;
    float r = length(p);
    vec4 tint = vertexColor * ColorModulator;
    float aa = max(fwidth(r), 0.001);
    float distance = abs(r - 0.86);
    // Two-pixel AA contours. The footprint geometry itself keeps its real world dimensions.
    float outer = stroke(distance, max(aa * 1.1, 0.003), aa);
    float angle = distance < aa * 5.0 + 0.01 ? atan(p.y, p.x + 0.000001) : 0.0;
    float detail = 0.0;
    float falloff = max(0.0, 1.0 - r / 0.86);
    float fill = falloff * 0.11;
    if (markerStyle == 15 || markerStyle == 16) {
        // A continuous readable perimeter with subtle dashed accents, and a visible tinted interior.
        float dash = smoothstep(-0.1, 0.2, cos(angle * (markerStyle == 15 ? 24.0 : 32.0)));
        outer *= 0.70 + 0.30 * dash;
        fill = sqrt(falloff) * (markerStyle == 16 ? 0.23 : 0.18);
        if (markerStyle == 16) detail = stroke(abs(r - 0.74), max(aa * 0.6, 0.002), aa) * 0.40;
    } else if (markerStyle == 17) {
        outer *= 0.55 + 0.45 * smoothstep(0.05, 0.3, abs(sin(angle * 2.0 + 0.45)));
        detail = stroke(abs(r - 0.67), max(aa * 0.65, 0.002), aa) * 0.55;
    } else if (markerStyle == 18) {
        detail = stroke(abs(r - 0.59), max(aa * 0.65, 0.003), aa) * 0.55;
        fill = falloff * 0.16;
    } else if (markerStyle == 19 || markerStyle == 20) {
        outer *= 0.65 + 0.35 * smoothstep(-0.1, 0.2, cos(angle * (markerStyle == 19 ? 3.0 : 8.0)));
        detail = stroke(abs(r - 0.62), max(aa * 0.6, 0.002), aa) * 0.45;
    }
    float ink = max(outer, detail);
    float rim = stroke(distance, max(aa * 2.0, 0.009), aa) * 0.68;
    float strokes = max(ink, rim);
    float coverage = max(strokes, fill);
    if (coverage < 0.001) discard;
    // Keep coverage colored; the old dim interior accidentally inherited the dark outline color.
    vec3 color = strokes > fill ? mix(vec3(0.025, 0.04, 0.055), tint.rgb, min(1.0, ink / max(strokes, 0.001))) : tint.rgb;
    fragColor = vec4(color, coverage * tint.a);
}
