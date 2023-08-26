/**
 * This module will:
 *
 * 1. Create an initialized axios instance
 *
 * 2. Add request interceptor to log request info
 *
 * 3. Add response interceptor to handle some global response codes
 */
import axios from 'axios';
import { Message } from 'element-ui';
import { PageLocation } from '@/ts/dynamicLocation';
import globalHandledRespCodes, {
    SUCCESS,
} from '@/ts/GlobalHandledResponseCode';
import {Notification} from "element-ui";

// create an axios instance
const service = axios.create({
    baseURL: new PageLocation().baseURL, // url = base url + request url
    // withCredentials: true, // send cookies when cross-domain requests
    timeout: 5000, // request timeout
});

// request interceptor
service.interceptors.request.use(
    (config) => {
        let logtag = `Network Request: =====> ${config.url}\n`;
        console.log(logtag, config);
        return config;
    },
    (error) => {
        // do something with request error
        console.log(error); // for debug
        return Promise.reject(error);
    },
);

// response interceptor
service.interceptors.response.use(
    /**
     * If you want to get http information such as headers or status
     * Please return  response => response
     */

    /**
     * Determine the request status by custom code
     * Here is just an example
     * You can also judge the status by HTTP Status Code
     */
    (response) => {
        /**
         * {
         *      code: "",
         *      data: {},
         *      msg: ""
         * }
         */
        const res = response.data;
        let relativeUrl = response.config.url!.substring(
            response.config.baseURL!.length,
        );
        let logtag = `Network Response: <==== ${relativeUrl}\n`;
        console.log(logtag, res);
        let code = res.code;

        // success
        if (code === SUCCESS) {
            return res;
        }

        // if blob, return the blob to specific request "then" handler for downloading
        if (res.code == undefined) {
            return response;
        }

        // handle global error
        globalHandledRespCodes.handle(code);

        // give a chance to handle error by specific request "then" handler
        return Promise.reject(res);
    },
    (error) => {
        let response = error.response;
        if (response == null) {
            Notification.error('Server unreachable');
            return Promise.reject();
        }
        let status = response.status;
        let data = response.data;
        Notification.error(`${status}: ${data}`);
        return Promise.reject(response);
    },
);

export default service;
