import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import Topbar from '../topbar/topbar';
import Sidebar from '../sidebar/sidebar';

@Component({
  selector: 'app-shell',
  imports: [CommonModule,Topbar,Sidebar,RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export default class Shell {

}
