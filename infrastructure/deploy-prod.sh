#!/bin/bash
cp ../target/build/app.jar ansible/files/app.jar
cd ansible
ansible-playbook -i prod deploy.yml
