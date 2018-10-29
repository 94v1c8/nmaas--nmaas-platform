import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {ModalComponent} from "../../../shared/modal";
import {AppInstanceService} from "../../../service";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'nmaas-modal-app-restart',
  templateUrl: './app-restart-modal.component.html',
  styleUrls: ['./app-restart-modal.component.css'],
    providers:[ModalComponent]
})
export class AppRestartModalComponent implements OnInit {

    @ViewChild(ModalComponent)
    public readonly modal: ModalComponent;

    @Input()
    private appInstanceId: number;

    @Input()
    private domainId: number;

    constructor(private appInstanceService:AppInstanceService, private translate:TranslateService) {
        const browserLang = translate.currentLang == null ? 'en' : translate.currentLang;
        translate.use(browserLang.match(/en|fr|pl/) ? browserLang : 'en');
    }

    ngOnInit() {

    }

    public show(){
        this.modal.show();
    }

    public restart(){
        this.appInstanceService.restartAppInstance(this.appInstanceId, this.domainId).subscribe(suc=>this.modal.hide());
    }
}
