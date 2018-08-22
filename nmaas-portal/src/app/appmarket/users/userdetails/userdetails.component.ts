import {User} from '../../../model/user';
import {UserService} from '../../../service/user.service';
import {BaseComponent} from '../../../shared/common/basecomponent/base.component';
import {ComponentMode} from '../../../shared/common/componentmode';
import {Component, OnInit, Input} from '@angular/core';
import {Router, ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {isUndefined} from 'util';
import {AuthService} from "../../../auth/auth.service";
import {Debugger} from 'inspector';

@Component({
  selector: 'app-userdetails',
  templateUrl: './userdetails.component.html',
  styleUrls: ['./userdetails.component.css']
})
export class UserDetailsComponent extends BaseComponent implements OnInit {

  private userId: number;
  private user: User;

  constructor(private userService: UserService, private router: Router,
    private route: ActivatedRoute,
    private location: Location, private authService:AuthService) {
    super();
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (!isUndefined(params['id'])) {
        this.userId = +params['id'];  
        this.userService.getOne(this.userId).subscribe((user) => this.user = user);
      }
    });
    this.mode = this.getMode(this.route);
  }

  public onPasswordSubmit($event): void {
    this.userService.changePassword(this.user.id, $event).subscribe(() => {});
  }

  public onSave($event) {
    debugger;
    const user: User = $event;

    if (!user) {
       return;
    }

    if(user.id) {
        Debugger;
      this.userService.updateUser(user.id, user).subscribe((value) => this.router.navigate(['/users/view/', user.id]));
    } else {     
      this.userService.addUser(user.username).subscribe((id) => this.router.navigate(['/users/view/', id.id]));
    }
  }

  public remove(userId:number){
    this.userService.deleteOne(userId).subscribe((value) => this.router.navigate(['/users/']));
  }
}
