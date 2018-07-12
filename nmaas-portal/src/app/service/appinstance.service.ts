import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {isUndefined} from 'util';

import {AppConfigService} from '../service/appconfig.service';

import {Id} from '../model/id';
import {AppInstanceStatus} from '../model/appinstancestatus';
import {AppInstance, AppInstanceRequest} from '../model/appinstance';
import {AppInstanceState} from '../model/appinstancestatus';
import {AppInstanceProgressStage} from '../model/appinstanceprogressstage';
import { GenericDataService } from './genericdata.service';

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/map'
import 'rxjs/add/operator/timeout';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';

@Injectable()
export class AppInstanceService extends GenericDataService {

  constructor(http: HttpClient, appConfig: AppConfigService) {
    super(http, appConfig);
  }

  public getAllAppInstances(domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId));
  }

  public getMyAppInstances(domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId) + 'my');
  }

  public getUserAppInstances(username: string, domainId?: number): Observable<AppInstance[]> {
    return this.get<AppInstance[]>(this.getUrl(domainId) + 'user/' + username);
  }

  public getAppInstanceState(id: Number, domainId?: number): Observable<AppInstanceStatus> {
    return this.get<AppInstanceStatus>(this.getUrl(domainId) + id + '/state');
  }

  public createAppInstance(domainId: number, appId: number, name: string): Observable<Id> {
    return this.post<AppInstanceRequest, Id>(this.getUrl(domainId), new AppInstanceRequest(appId, name));
  }

  public removeAppInstance(appInstanceId: Number, domainId?: number): Observable<any> {
    return this.delete<any>(this.getUrl(domainId) + appInstanceId);      
  }

  public getAppInstance(appInstanceId: Number, domainId?: number): Observable<AppInstance> {
    return this.get<AppInstance>(this.getUrl(domainId) + appInstanceId);
  }

  public applyConfiguration(appInstanceId: Number, configuration: string, domainId?: number): Observable<void> {
    return this.post<String, any>(this.getUrl(domainId) + appInstanceId + '/configure', configuration);                
  }

  protected getUrl(domainId?: number): string {
    if (isUndefined(domainId)) {
      return this.appConfig.getApiUrl() + '/apps/instances/';
    } else {
      return this.appConfig.getApiUrl() + '/domains/' + domainId + '/apps/instances/';
    }
  }

  public getProgressStages(): AppInstanceProgressStage[] {
    return [
      new AppInstanceProgressStage('Subscription validation', AppInstanceState.VALIDATION),
      new AppInstanceProgressStage('Environment creation', AppInstanceState.PREPARATION),
      new AppInstanceProgressStage('Setting up connectivity', AppInstanceState.CONNECTING),
      new AppInstanceProgressStage('Applying app configuration', AppInstanceState.CONFIGURATION_AWAITING),
      new AppInstanceProgressStage('App container deployment', AppInstanceState.DEPLOYING),
      new AppInstanceProgressStage('App running', AppInstanceState.RUNNING),
      new AppInstanceProgressStage('Undeploying', AppInstanceState.UNDEPLOYING, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE]),
      new AppInstanceProgressStage('Removed', AppInstanceState.DONE, [AppInstanceState.UNDEPLOYING, AppInstanceState.DONE])
    ];
  }

  public restartAppInstance(appInstanceId:number, domainId?: number):Observable<any>{
    return this.post<number,any>((this.getUrl(domainId) +  appInstanceId + '/restart'), appInstanceId);
  }

}
