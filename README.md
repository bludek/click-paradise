# Click Paradise

Demonstrate a possible continuous integration/deployment infrastructure for a web application.

## Pipeline
Deployment is achieved by completing 3 major steps :

> - Build src
> - Deploy in UAT environment
> - Deploy in PROD environment (approval required)

## Which tools are used ?
> - ***Vagrant*** : simulates different hosts
> - ***Ansible*** : hosts provisioning
> - ***Jenkins*** : Pipeline orchestrator
> - ***Docker*** : will do a lot of thing such as running our app in containers


## Infrastructure
Vagrant will spawn 7 VMs
> VM          | IP
> ----------- | ---
> Jenkins     | 10.100.192.200
> UAT-master  | 10.110.192.200
> UAT-node-1  | 10.110.192.101
> UAT-node-2  | 10.110.192.102
> PROD-master | 10.120.192.200
> PROD-node-1 | 10.120.192.101
> PROD-node-2 | 10.120.192.102

Simulating

> - Jenkins + custom docker registry
> - UAT docker swarm cluster (master + 2 nodes)
> - PROD docker swarm cluster (master + 2 nodes)
> - Nginx proxy for a 0 downtime frontend

Here is a sample schema of the Infrastructure.

> ![Alt text](http://g.gravizo.com/g?
digraph G {
   aize ="4,4";
   Jenkins [shape=box];
   UAT [shape=box];
   PROD [shape=box];
   pNode1 [shape=box];
   pNode2 [shape=box];
   uNode1 [shape=box];
   uNode2 [shape=box];
   PROD -> {pNode1; pNode2}
   UAT -> {uNode1; uNode2}
   Jenkins -> {Build; Deploy};
   Build -> Deploy
   Deploy -> UAT
   Deploy -> PROD [color=red,label="Approval"]
 })


## How to use it ?
You can deploy the infrastructure by running :
```
./infra/infra.sh
```
**This may take some time.**

But this will :
> - Spwan VMs
> - Upgrage kernel
> - Reload VMs
> - Launch Ansible playbooks

Update your hosts so you can access frontends
```
10.110.192.200 	uat.clickparadise.fr
10.120.192.200 	prod.clickparadise.fr
```

#### Jenkins
You can access Jenkins in your web browser
```
http://10.100.192.200:8080/
```

#### ClickCount
You can now access the app at :
```
http://uat.clickparadise.fr/clickCount/

http://prod.clickparadise.fr/clickCount/
```
#### Consul (cluster backend)
```
http://uat.clickparadise.fr:8500

http://prod.clickparadise.fr:8500
```

## Troubleshooting
