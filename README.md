## Install Ambari NiFi Service
https://github.com/abajwa-hw/ambari-nifi-service

## Add the 9090 port forwarding rule to your VirtualBox
### Find the HDP Sandbox VM id:

```
~ ♨ >  VBoxManage list vms
"boot2docker-vm" {2e7c902c-34fa-45e9-863b-313beb2b4b33}
"centos64_c6401_1440615279050_15827" {b3f9d4a5-a9cf-4495-90ff-139240a11be7}
"NiFi" {10a766b0-358c-4f33-8d66-f2fc3bd8d770}
"docker-vm" {06e9e8ec-929a-4968-96f0-4f54e20b4cec}
"default" {0fedc097-a65a-47d0-90ab-399824ef069f}
"Hortonworks Sandbox with HDP 2.3" {ab54bfd3-a33b-4844-88c3-924f7df9498a}
```

### Create a rule for the target VM (replace with your ID)

```
~ ♨ >  VBoxManage controlvm ab54bfd3-a33b-4844-88c3-924f7df9498a natpf1 nifi,tcp,,9090,,9090
```


## Maven reference documentation for custom NiFi processors
https://cwiki.apache.org/confluence/display/NIFI/Maven+Projects+for+Extensions

### Full command for project generation (non-interactive)

```
mvn archetype:generate \
    -DarchetypeGroupId=org.apache.nifi \
    -DarchetypeArtifactId=nifi-processor-bundle-archetype \
    -DarchetypeVersion=0.3.0 \
    -DnifiVersion=0.3.0 \
    -DartifactBaseName=workshop-demo \
    -Dpackage=com.hortonworks.iot.demo

### Update maven central indices if applicable (e.g. in IntelliJ IDEA)

### Fix the top-level pom parent
Change the top-level pom declaration to update the parent version to 0.3.0

### Build the project

```
mvn clean install
```
The NAR package will be placed in the `nifi-workshop-demo-nar/target` directory.

## Upload the NAR via Ambari
Ambari -> Local Files
Upload to the `/opt/nifi-*/lib`

*If you can't go down into the `lib` directory*: right click on `lib -> Permissions -> RWX for 'others'`

### Re-deploy
* Delete the old NAR file
* Upload a new version
* Restart the NiFI via `Ambari -> NiFi -> Restart All`

But it's painful if you have to repeat it many times. Best practice - extensive NiFi unit testing of components and processors in a local IDE.
 See the testcase in the project.


# Misc
* Example of a processor implementation
https://github.com/apache/nifi/blob/master/nifi-external/nifi-example-bundle/nifi-nifi-example-processors/src/main/java/org/apache/nifi/processors/WriteResourceToStream.java

* Example of a processor testcase
https://github.com/apache/nifi/blob/master/nifi-nar-bundles/nifi-standard-bundle/nifi-standard-processors/src/test/java/org/apache/nifi/processors/standard/TestRouteOnAttribute.java

* Default NiFi bundles
https://github.com/apache/nifi/tree/master/nifi-nar-bundles

* Final result
https://github.com/aperepel/nifi-workshop/tree/master

# Extra Credit

- Send jokes to a Kafka topic. Different Kafka topics? (Flow design)
- Improve user experience by restricting allowable values for properties (e.g. true/false, pre-defined buffer sizes) (Coding)
- Accumulate jokes to be written in one file, e.g. as a batch of 20 or every 30 seconds (MergeContent Processor)