import {throwError as observableThrowError,  Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Id, Application, Rate, Comment, FileInfo } from '../model';
import { AppConfigService } from './appconfig.service';
import { GenericDataService } from './genericdata.service';
import {catchError, debounceTime} from 'rxjs/operators';
import {AppStateChange} from '../model/appstatechange';
import {isNullOrUndefined} from 'util';
import {ApplicationBase} from '../model/application-base';

@Injectable()
export class AppsService extends GenericDataService {

    constructor(http: HttpClient, appConfig: AppConfigService) {
      super(http, appConfig);
    }


    public getApps(): Observable<ApplicationBase[]> {
        return this.get<ApplicationBase[]>(this.appConfig.getApiUrl() + '/apps/base');
    }

    public getAllApps(): Observable<ApplicationBase[]> {
        return this.get<ApplicationBase[]>(this.appConfig.getApiUrl() + '/apps/base/all');
    }

    public getApp(id: number): Observable<Application> {
        return this.get<Application>(this.appConfig.getApiUrl() + '/apps/' + id);
    }

    public getBaseApp(id: number): Observable<ApplicationBase> {
        return this.get<ApplicationBase>(this.appConfig.getApiUrl() + '/apps/base/' + id);
    }

    public getAppRateByUrl(urlPath: string): Observable<Rate> {
        if (!isNullOrUndefined(urlPath) && urlPath !== '') {
            return this.getByUrl(urlPath);
        }
    }

    public setMyAppRateByUrl(urlPath: string): Observable<any> {
        return this.http.post(this.appConfig.getApiUrl() + urlPath, null).pipe(
            debounceTime(10000),
            catchError((error: any) => observableThrowError((typeof error.json === 'function' ? error.json().message : 'Server error'))));
    }

    public getAppCommentsByUrl(urlPath: string): Observable<Comment[]> {
        return this.getByUrl(urlPath);
    }

    public addAppCommentByUrl(urlPath: string, comment: Comment): Observable<Id> {
        return this.http.post<Id>(this.appConfig.getApiUrl() + urlPath, comment).pipe(
            debounceTime(10000),
            catchError((error: any) => observableThrowError((typeof error.json === 'function' ? error.json().message : 'Server error'))));
    }

    public deleteAppCommentByUrl(urlPath: string, id: Id): Observable<any> {
        return this.http.delete(this.appConfig.getApiUrl() + urlPath + '/' + id.id).pipe(
            debounceTime(10000),
            catchError((error: any) => observableThrowError((typeof error.json === 'function' ? error.json().message : 'Server error'))));
    }

    public getAppScreenshotsByUrl(urlPath: string): Observable<FileInfo[]> {
        return this.getByUrl(urlPath);
    }

    public getAppScreenshotUrl(urlPath: string): string {
        return this.appConfig.getApiUrl() + urlPath;
    }

    public deleteApp(appId: number): Observable<any> {
        return this.delete(this.appConfig.getApiUrl() + '/apps/' + appId);
    }

    public addApp(app: Application): Observable<any> {
        return this.post(this.appConfig.getApiUrl() + '/apps', app);
    }

    public updateApp(app: Application): Observable<any> {
        return this.patch(this.appConfig.getApiUrl() + '/apps', app);
    }

    public updateBaseApp(app: ApplicationBase): Observable<any> {
        return this.patch(this.appConfig.getApiUrl() + '/apps/base', app);
    }

    public getLatestVersion(appName: string): Observable<any> {
        return this.get(this.appConfig.getApiUrl() + '/apps/' + appName + '/latest');
    }

    public uploadAppLogo(id: number, file: any): Observable<FileInfo> {
        const fd: FormData = new FormData();
        fd.append('file', file);
        return this.post(this.appConfig.getApiUrl() + '/apps/' + id + '/logo', fd);
    }

    public uploadScreenshot(id: number, file: any): Observable<FileInfo> {
        const fd: FormData = new FormData();
        fd.append('file', file);
        return this.post(this.appConfig.getApiUrl() + '/apps/' + id + '/screenshots', fd);
    }

    public changeApplicationState(id: number, appStateChange: AppStateChange): Observable<any> {
        return this.patch(this.appConfig.getApiUrl() + '/apps/state/' + id, appStateChange);
    }

    private getByUrl(urlPath: string): Observable<any> {
        return this.http.get(this.appConfig.getApiUrl() + urlPath).pipe(
            debounceTime(10000),
            catchError((error: any) => observableThrowError((typeof error.json === 'function' ? error.json().message : 'Server error'))));
    }
}
