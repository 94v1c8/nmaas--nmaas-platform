import { Application } from '../model/application';
import { AppSubscription } from '../model/appsubscription';
import { AppConfigService } from './appconfig.service';
import { GenericDataService } from './genericdata.service';
import { JsonMapperService } from './jsonmapper.service';
import { Injectable } from '@angular/core';
import { AuthHttp } from 'angular2-jwt';
import { Observable } from 'rxjs/Observable';
import { isUndefined } from 'util';

@Injectable()
export class AppSubscriptionsService extends GenericDataService {

  constructor(authHttp: AuthHttp, appConfig: AppConfigService, private jsonModelMapper: JsonMapperService) {
    super(authHttp, appConfig);
  }

  public subscribe(domainId: number, applicationId: number): Observable<any> {
    return this.post<AppSubscription, any>(this.getSubscriptionsUrl(), new AppSubscription(domainId, applicationId));
  }
  
  public subscribeRequest(domainId: number, applicationId: number): Observable<any> {
    return this.post<AppSubscription, any>(this.getSubscriptionsUrl()  + '/request', new AppSubscription(domainId, applicationId));
  }
  
  public unsubscribe(domainId: number, applicationId: number): Observable<any> {
    return this.delete<any>(this.getSubscriptionUrl(applicationId, domainId));
  }
  
  public getAll(): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getSubscriptionsUrl())
                .map((appSubscriptions) => this.jsonModelMapper.deserialize(appSubscriptions, AppSubscription));
  }
  
  public getAllByApplication(applicationId: number): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getApplicationSubscriptionsUrl(applicationId))
                .map((appSubscriptions) => this.jsonModelMapper.deserialize(appSubscriptions, AppSubscription));
  }

  public getAllByDomain(domainId: number): Observable<AppSubscription[]> {
    return this.get<AppSubscription[]>(this.getDomainSubscriptionsUrl(domainId))
                .map((appSubscriptions) => this.jsonModelMapper.deserialize(appSubscriptions, AppSubscription));
  }
  
  public getSubscription(applicationId: number, domainId: number): Observable<AppSubscription> {
    return this.get<AppSubscription>(this.getSubscriptionUrl(applicationId, domainId))
                .map((appSubscription) => this.jsonModelMapper.deserialize(appSubscription, AppSubscription));
  }
    
  public getSubscribedApplications(domainId?: number): Observable<Application[]> {
      return this.get<Application[]>(
                      (isUndefined(domainId) ? this.getSubscriptionsUrl() : this.getDomainSubscriptionsUrl(domainId)) + '/apps')
                 .map((applications) => this.jsonModelMapper.deserialize(applications, Application));
  }
  
  protected getUrl(): string {
    return this.appConfig.getApiUrl();
  }
    
  protected getSubscriptionsUrl(): string {
    return this.getUrl() + '/subscriptions';
  }
  
  protected getDomainSubscriptionsUrl(domainId: number): string {
    return this.getSubscriptionsUrl() + '/domains/' + domainId;
  }
  
  protected getApplicationSubscriptionsUrl(applicationId: number): string {
    return this.getSubscriptionsUrl() + '/apps/' + applicationId;
  }
  
  protected getSubscriptionUrl(applicationId: number, domainId: number): string {
    return this.getSubscriptionsUrl() + '/apps/' + applicationId + '/domains/' + domainId;
  }
  
}
