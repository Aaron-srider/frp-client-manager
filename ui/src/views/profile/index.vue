<template>
    <div class=''>
        <el-table :data="profileList">
            <el-table-column>
                <template #default="scope">
                    {{ scope.row.name }}
                </template>
            </el-table-column>     <el-table-column>
            <template #default="scope">
                <el-button @click="toProfileDetail(scope.row)">check</el-button>
            </template>
        </el-table-column>
        </el-table>

    </div>
</template>

<script lang='ts'>
import {Component, Vue} from 'vue-property-decorator';
import Client from "@/request/client";

@Component({})
export default class ProfileView extends Vue {
    profileList: any[] = []
    toProfileDetail(row: any) {
        this.$router.push({path: '/profile/detail', query:{
            profileName: row.name
            }})
    }
    created() {
        this.getProfileList()
    }

    getProfileList() {
        Client.getProfileList().then((res: any) => {
            this.profileList = res.data
        })

    }
}
</script>
<style lang='scss' scoped>
</style>
