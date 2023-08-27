<template>
    <div class=''>
        <el-card>
            <el-button @click="startFrpc">restart frpc</el-button>
            <el-button @click="refreshLog">refresh log</el-button>
            <el-input type="textarea" v-model="frpcLog" readonly :autosize="{ minRows: 2, maxRows: 10 }"></el-input>
        </el-card>
        <el-card>
            <div v-if="!common_config_set">
                common block is not configured
            </div>

            server_addr
            <el-input v-model="common_config.server_addr"></el-input>

            server_port
            <el-input v-model="common_config.server_port"></el-input>

            token
            <el-input v-model="common_config.token"></el-input>

            <el-button @click="editCommonConfig">submit</el-button>
        </el-card>
        <el-button @click="openAddClientConfigDialog">add</el-button>
        <el-table :data="clientBlockList">
            <el-table-column
                label="name"
            >
                <template #default="scope">
                    {{ scope.row.name }}
                </template>
            </el-table-column>
            <el-table-column
                label="type"
            >
                <template #default="scope">
                    {{ scope.row.type }}
                </template>
            </el-table-column>
            <el-table-column
                label="local_ip"
            >
                <template #default="scope">
                    {{ scope.row.local_ip }}
                </template>
            </el-table-column>
            <el-table-column
                label="local_port"
            >
                <template #default="scope">
                    {{ scope.row.local_port }}
                </template>
            </el-table-column>
            <el-table-column
                label="remote_port"
            >
                <template #default="scope">
                    {{ scope.row.remote_port }}
                </template>
            </el-table-column>
            <el-table-column
                label="op"
            >
                <template #default="scope">
                    <el-button @click="openEditClientConfig(scope.row)">
                        <i class="el-icon-edit"></i>
                    </el-button>
                    <el-button @click="deleteClientConfig(scope.row)">
                        <i class="el-icon-delete "></i>
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog :visible.sync="editClientConfigDialogVisible">
            name {{ editClientConfigDialogData.name }}
            type
            <el-input v-model="editClientConfigDialogData.type"></el-input>
            local_ip
            <el-input v-model="editClientConfigDialogData.local_ip"></el-input>
            local_port
            <el-input v-model="editClientConfigDialogData.local_port"></el-input>
            remote_port
            <el-input v-model="editClientConfigDialogData.remote_port"></el-input>
            <el-button @click="editClientConfig">submit</el-button>
        </el-dialog>





        <el-dialog :visible.sync="addClientConfigDialogVisible">
            name
            <el-input v-model="addClientConfigDialogData.name"></el-input>
            type
            <el-input v-model="addClientConfigDialogData.type"></el-input>
            local_ip
            <el-input v-model="addClientConfigDialogData.local_ip"></el-input>
            local_port
            <el-input v-model="addClientConfigDialogData.local_port"></el-input>
            remote_port
            <el-input v-model="addClientConfigDialogData.remote_port"></el-input>
            <el-button @click="addClientConfig">submit</el-button>
        </el-dialog>
    </div>
</template>

<script lang='ts'>
import {Component, Vue} from 'vue-property-decorator';
import Client from "@/request/client";
import {Notification} from "element-ui";
import {PageLocation} from "@/ts/dynamicLocation";

@Component({})
export default class IndexView extends Vue {
    clientBlockList: any[] = [];

    common_config_set = false

    common_config: any = {
        server_addr: '',
        server_port: 0,
        token: ''
    }

    editClientConfigDialogVisible: boolean = false
    editClientConfigDialogData: any = {

    }

    frpcLog: string = ''

    webSocketClient: WebSocket | null = null;

    addClientConfigDialogVisible: boolean = false

    addClientConfigDialogData: any = {
        name: '',
        type: '',
        local_ip: '',
        local_port: 0,
        remote_port: 0
    }

    openAddClientConfigDialog(row:any) {
        this.addClientConfigDialogVisible = true
        this.addClientConfigDialogData = {...row}
    }

    addClientConfig() {
        Client.addClientConfig(this.addClientConfigDialogData).then((resp: any) => {
            Notification.success('add client config success')
            this.clientBlockList.push(resp.data)
            this.addClientConfigDialogVisible = false
        })
    }

    webSocketAlive() {
        return this.webSocketClient != null && this.webSocketClient.readyState == WebSocket.OPEN
    }

    startFrpc() {
        Client.startFrpc().then((res: any) => {
            if (!this.webSocketAlive()) {
                this.startWebSocket()
            }
        })
    }

    startWebSocket() {

        this.webSocketClient = new WebSocket('wss://frpui.wenchao.fit/frpc-mananger');

        this.webSocketClient.onopen = () => {
            console.log('WebSocketClient connected');
            setInterval(() => {
                this.webSocketClient?.send('frpc-process-log')
                console.log('WebSocketClient send command')
            }, 1000)
        };

        this.webSocketClient.onerror = (error) => {
            console.log('WebSocketClient error: ', error);
        };

        this.webSocketClient.onclose = () => {
            console.log('WebSocketClient closed');
        };

        this.webSocketClient.onmessage = (message) => {
            // console.log('WebSocketClient received message: ', message.data);
            this.frpcLog = message.data
        };
    }

    refreshLog() {
        if (!this.webSocketAlive()) {
            this.startWebSocket()
        }
    }

    editCommonConfig() {

        if (!this.isIpAddress(this.common_config.server_addr)) {
            Notification.error("server_addr is not a valid ip address")
            return
        }

        if (!this.isPort(this.common_config.server_port)) {
            Notification.error("server_port is not a valid port")
            return
        }

        if (this.common_config.token == '') {
            Notification.error("token is empty")
            return
        }

        Client.editCommonConfig(this.common_config).then((res: any) => {
            Notification.success("edit successfully")
            this.common_config_set = true
        })
    }

    isPositiveInteger(number: number): boolean {
        return Number.isInteger(number) && number > 0;
    }

    isIpAddress(ipAddress: string): boolean {
        return (/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/
            .test(ipAddress))
    }

    isPort(port: number): boolean {
        return 0 < port && port <= 65535
    }

    openEditClientConfig(clientConfig: any) {
        this.editClientConfigDialogVisible = true
        this.editClientConfigDialogData = {...clientConfig}
    }

    deleteClientConfig(row: any) {
        Client.deleteClientConfig(row.name).then(resp => {
            this.clientBlockList = this.clientBlockList.filter(item => item.name !== row.name)
            Notification.success("delete successfully")
        })
    }

    editClientConfig() {
        if (!this.isPort(Number(this.editClientConfigDialogData.local_port))) {
            Notification.error("local_port must be port")
            return
        }

        if (!this.isPort(Number(this.editClientConfigDialogData.remote_port))) {
            Notification.error("remote_port must be port")
            return
        }

        if (!this.isIpAddress(this.editClientConfigDialogData.local_ip)) {
            Notification.error("local_ip must be ip address")
            return
        }

        if (this.editClientConfigDialogData.type != "tcp" && this.editClientConfigDialogData.type != "udp") {
            Notification.error("type must be tcp or udp")
            return
        }

        let clientConfig = this.editClientConfigDialogData
        Client.editClientConfig(clientConfig).then(resp => {
            this.clientBlockList.filter(item => item.name === clientConfig.name).forEach(item => {
                item.local_ip = clientConfig.local_ip
                item.type = clientConfig.type
                item.local_port = clientConfig.local_port
                item.remote_port = clientConfig.remote_port
            })
            Notification.success("edit successfully")
            this.editClientConfigDialogVisible = false
        })
    }

    created() {
        Client.getCommonConfig().then(resp => {
            this.common_config = resp.data
            this.common_config_set = true
            return Client.getClientBlockList()
        })
            .then((resp: any) => {
                this.clientBlockList = resp.data
            }, (err: any) => {
                this.common_config_set = false
            })

    }
}
</script>
<style lang='scss' scoped>
</style>
