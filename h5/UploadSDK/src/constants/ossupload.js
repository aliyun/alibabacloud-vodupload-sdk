//Copyright (C) 2020 Alibaba Group Holding Limited
export const  UPLOADSTATE = {
        INIT : "init",
        UPLOADING: "uploading",
        COMPLETE : "complete",
        INTERRUPT : "interrupt"
    };

export const  UPLOADSTEP = {
        INIT : "init",
        PART : "part",
        COMPLETE: "complete"
    };

export const UPLOADDEFAULT = {
	PARALLEL : 5,
    PARTSIZE : 1048576
}
