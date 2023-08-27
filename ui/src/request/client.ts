import request from '@/request';
import { AxiosPromise } from 'axios';

class Client {
    // region: Playground Project Template
    static createPlaygroundProjectTemplateFromDirectory(
        path: string,
        name: string,
    ): AxiosPromise<any> {
        return request({
            url: `/playground-project-template/from-directory`,
            method: 'post',
            data: {
                path,
                name,
            },
        });
    }
    static uploadPlaygroundProjectTemplate(
        formData: FormData,
    ): AxiosPromise<any> {
        return request({
            url: `/playground-project-template`,
            method: 'post',
            data: formData,
        });
    }
    static updatePlaygroundProjectTemplateBinary(
        playgroundProjectTemplateId: number,
        formData: FormData,
    ): AxiosPromise<any> {
        return request({
            url: `/playground-project-template-binary/${playgroundProjectTemplateId}`,
            method: 'put',
            data: formData,
        });
    }

    static deletePlaygroundProjectTemplate(
        playgroundProjectTemplateId: number,
    ): AxiosPromise<any> {
        return request({
            url: `/playground-project-template/${playgroundProjectTemplateId}`,
            method: 'delete',
        });
    }

    static getPlaygroundProjectTemplateList(): AxiosPromise<any> {
        return request({
            url: `/playground-project-templates`,
            method: 'get',
        });
    }

    static downloadPlaygroundProjectTemplate(
        playgroundProjectTemplateId: number,
    ): AxiosPromise<any> {
        return request({
            url: `/playground-project-template/${playgroundProjectTemplateId}`,
            method: 'get',
            params: { playgroundProjectTemplateId },
            responseType: 'blob',
        });
    }
    // endregion


    static getClientBlockList(profileName: string ): AxiosPromise<any> {
        return request({
            url: `/client-configs`,
            method: 'get',
            params : {
                profileName
            }
        });
    }

    static editClientConfig(clientConfig: any) {
        return request({
            url: `/client-config`,
            method: 'put',
            data: clientConfig,
        });
    }

    static editCommonConfig(common_config: any) {
        return request({
            url: `/common-config`,
            method: 'put',
            data: common_config,
        });
    }

    static getCommonConfig(profileName: string
    ) {
        return request({
            url: `/common-config`,
            method: 'get',
            params: {
                profileName
            }
        });
    }

    static startFrpc() {
        return request({
            url: `/frpc`,
            method: 'get',
            params: {
                command: "restart"
            }
        });
    }

    static deleteClientConfig(name:string): AxiosPromise<any> {
        return request({
            url: `/client-config`,
            method: 'delete',
            data: {
                name
            }
        });
    }

    static addClientConfig(addClientConfigDialogData: any): AxiosPromise<any> {
        return request({
            url: `/client-config`,
            method: 'post',
            data: addClientConfigDialogData
        });
    }

    static getProfileList() {
        return request({
            url: `/profiles`,
            method: 'get',
        });
    }
}

export default Client;
