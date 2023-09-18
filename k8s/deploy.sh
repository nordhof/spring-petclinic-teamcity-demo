#!/usr/bin/env bash

cat << EOF > k8s/patch/patch.json
[{
  "op": "replace",
  "path": "/spec/template/spec/containers/0/image",
  "value": "ghcr.io/nordhof/spring-petclinic-teamcity-demo:${IMAGE_ID}"
}]
EOF

kubectl kustomize k8s/patch
