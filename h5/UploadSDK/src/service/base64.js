//Copyright (C) 2020 Alibaba Group Holding Limited
export default class Base64
{
    static encode(string) {
      return new Buffer(string).toString('base64');
    }

    static decode(string) {
      return new Buffer(string, 'base64').toString();
    }

}