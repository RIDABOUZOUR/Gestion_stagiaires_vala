import { Component } from '@angular/core';
import { AppRoutingModule } from "../app-routing-module";
import { AppModule } from "../app-module";

@Component({
  selector: 'app-admin-template',
  standalone: true,
  imports: [AppRoutingModule, AppModule],
  templateUrl: './admin-template.component.html',
  styleUrl: './admin-template.component.css'
})
export class AdminTemplateComponent {

}
