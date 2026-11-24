#version 330
#extension GL_ARB_separate_shader_objects : require

// Analytic crosshair geometry in framebuffer pixels. Sampler0 supplies parameters.
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};
uniform sampler2D Sampler0;
layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;
layout(location = 0) out vec4 fragColor;

float boxDistance(vec2 p, vec2 halfSize) {
    vec2 q = abs(p) - halfSize;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0);
}

float shapeDistance(vec2 p, float thickness, float gap, float unit, int style) {
    float halfWidth = thickness * 0.5;
    if (style == 0) {
        float arm = max(1.0, floor(7.0 * unit + 0.5));
        return min(boxDistance(p, vec2(arm + halfWidth, halfWidth)),
                   boxDistance(p, vec2(halfWidth, arm + halfWidth)));
    }
    if (style == 1) {
        float halfLength = max(1.0, floor(5.0 * unit + 0.5)) * 0.5;
        float arm = halfWidth + gap + halfLength;
        return min(boxDistance(p, vec2(halfWidth)), min(
            boxDistance(vec2(abs(p.x) - arm, p.y), vec2(halfLength, halfWidth)),
            boxDistance(vec2(p.x, abs(p.y) - arm), vec2(halfWidth, halfLength))));
    }
    if (style == 2) {
        float dotRadius = max(0.5, thickness * 0.75);
        float radius = max(dotRadius + halfWidth + 0.5, 7.0 * unit - halfWidth);
        return min(abs(length(p) - radius) - halfWidth, length(p) - dotRadius);
    }
    vec2 end = vec2(7.0 * unit / sqrt(2.0));
    p.x = abs(p.x);
    return length(p - end * clamp(dot(p, end) / dot(end, end), 0.0, 1.0)) - halfWidth;
}

void main() {
    vec4 parameters = texelFetch(Sampler0, ivec2(0, 0), 0);
    vec4 fill = texelFetch(Sampler0, ivec2(1, 0), 0);
    vec4 outline = texelFetch(Sampler0, ivec2(2, 0), 0);
    int style = int(parameters.w + 0.5);
    float unit = texelFetch(Sampler0, ivec2(3, 0), 0).r;
    float radius = length(texCoord0);
    float pixel = length(vec2(dFdx(radius), dFdy(radius)));
    float d = style == 4 ? abs(radius - 1.0) - pixel * 0.75
        : shapeDistance(texCoord0, parameters.x, parameters.y, unit, style);
    float fillCoverage = d <= 0.00001 ? 1.0 : 0.0;
    float outerCoverage = d <= parameters.z + 0.00001 ? 1.0 : 0.0;
    float fade = vertexColor.a * ColorModulator.a;
    float f = fillCoverage * fill.a * fade;
    float o = max(outerCoverage - fillCoverage, 0.0) * outline.a * fade;
    int mode = int(vertexColor.r * 255.0 + 0.5);
    if (mode == 1) {
        if (f <= 0.0) discard;
        fragColor = vec4(vec3(f), 1.0);
    } else if (mode == 2) {
        if (o <= 0.0) discard;
        fragColor = vec4(outline.rgb * ColorModulator.rgb, o);
    } else {
        float alpha = f + o;
        if (alpha <= 0.0) discard;
        fragColor = vec4((fill.rgb * f + outline.rgb * o) / alpha * ColorModulator.rgb, alpha);
    }
}
