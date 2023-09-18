#!/usr/bin/env bash

cat << EOF > k8s/patch/patch.json
[{
  "op": "replace",
  "path": "/spec/template/spec/containers/0/image",
  "value": "ghcr.io/nordhof/spring-petclinic-teamcity-demo:${IMAGE_ID}"
}]
EOF

kubectl kustomize k8s/patch > k8s/final-deploy.yml
kubectl --kubeconfig kube-config apply -f k8s/final-deploy.yml
