#version 330
#extension GL_ARB_separate_shader_objects : require

// Single-channel SDF shape rendering (rounded rects, discs, soft shadows): Sampler0.r holds a
// signed distance field generated at runtime, so edges stay anti-aliased at any draw size.
// Same vertex interface as minecraft:core/position_tex_color.

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

// Distance range baked into the runtime shape atlas (see SdfShapeAtlas.PX_RANGE).
const float PX_RANGE = 8.0;

void main() {
    float sd = texture(Sampler0, texCoord0).r;

    // A 9-slice stretches tangential UVs by very different amounts. The distance
    // derivative measures only the edge normal, keeping coverage one screen pixel wide.
    float edgeWidth = max(fwidth(sd), 1.0 / 255.0);
    float alpha = clamp((sd - 0.5) / edgeWidth + 0.5, 0.0, 1.0);
    float outAlpha = vertexColor.a * alpha;
    if (outAlpha <= 0.004) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, outAlpha) * ColorModulator;
}
