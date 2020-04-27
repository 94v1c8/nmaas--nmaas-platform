import {ServiceStorageVolumeType} from "./servicestoragevolume";

export class AppStorageVolume {
    public type: ServiceStorageVolumeType;
    public defaultStorageSpace: number;
    public deployParameters: object = {}; // this should be Map<string, string> but JS cannot stringify object of this type
}
