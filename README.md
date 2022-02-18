# AUpdater
 DDNS client for DNSPod

## Usage
```shell
java -jar AUpdater.jar [configPath] [--test]
```
- `configPath`: path to the config file. Default is "./aupdater.properties"
- `--test`: check the config file and exit.

## Extra
- The [systemd service file](https://github.com/MCUmbrella/AUpdater/blob/main/aupdater.service) is included, and you can use it to start the client automatically.
Remember to check the location of the program and the config file. Change them if necessary.
- The detailed usage of DNSPod API can be found at https://docs.dnspod.cn/api/
