#version 460 core

#define lightColor vec3(1.0, 1.0, 1.0)
#define lightPos vec3(512.,512., 10000.0)
#define ambientStrength 0.1
#define specularStrength 0.2

out vec4 outColor;
in vec2 uvs;
in vec3 fragPos;

layout(location = 0) uniform vec3 cameraPos;

uniform sampler2D colormap;

void main()
{
    vec3 objectColor = texture(colormap,fragPos.xy/1024.).xyz;
    vec3 dpdu = dFdx(fragPos.xyz);
    vec3 dpdv = dFdy(fragPos.xyz);
    vec3 norm = normalize(cross(dpdu, dpdv));

    //ambient
    vec3 ambient = ambientStrength * lightColor;

    //difuse
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    //specular
    vec3 viewDir = normalize(cameraPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 256);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * objectColor;
    outColor = vec4(result, 1.0);
}