#version 330
#extension GL_ARB_separate_shader_objects : require

// MSDF glyph rendering: the RGB channels of Sampler0 hold a multi-channel signed distance
// field, so the edge softness is derived from the screen-space footprint of the UVs and stays
// exactly one pixel wide at any draw size or pose scale. Keep in sync with the vertex shader
// interface of minecraft:core/position_tex_color (varyings texCoord0 / vertexColor).

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

// Distance range baked into the atlas (msdf-atlas-gen -pxrange 4).
const float PX_RANGE = 4.0;

float median3(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msd = texture(Sampler0, texCoord0).rgb;
    float sd = median3(msd.r, msd.g, msd.b);

    vec2 atlasSize = vec2(textureSize(Sampler0, 0));
    vec2 unitRange = vec2(PX_RANGE) / atlasSize;
    vec2 screenTexSize = vec2(1.0) / fwidth(texCoord0);
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);

    float alpha = clamp(screenPxRange * (sd - 0.5) + 0.5, 0.0, 1.0);
    float outAlpha = vertexColor.a * alpha;
    if (outAlpha <= 0.004) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, outAlpha) * ColorModulator;
}
