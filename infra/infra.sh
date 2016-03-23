#!/bin/bash

set -e

vagrant up UAT-master UAT-node-1 UAT-node-2 PROD-master PROD-node-1 PROD-node-2

vagrant reload UAT-master UAT-node-1 UAT-node-2 PROD-master PROD-node-1 PROD-node-2

vagrant ssh UAT-master -c 'ansible-playbook /vagrant/ansible/swarm-uat.yml -i /vagrant/ansible/hosts/prod \'
vagrant ssh PROD-master -c 'ansible-playbook /vagrant/ansible/swarm-prod.yml -i /vagrant/ansible/hosts/prod \'
vagrant ssh jenkins -c 'ansible-playbook /vagrant/ansible/jenkins.yml -i /vagrant/ansible/hosts/prod \
    && ansible-playbook /vagrant/ansible/interlock.yml -i /vagrant/ansible/hosts/prod'
