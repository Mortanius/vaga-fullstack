import { Component, ElementRef, Self } from '@angular/core';

export interface Alert {
  type: string;
  message: string;
}

@Component({
    selector: 'ngbd-alert-closeable',
    templateUrl: './alert-closeable.html',
    styleUrls: ['./alert.component.css']
  })
  export class NgbdAlertCloseable {

    constructor( @Self() private element: ElementRef) { }
  
    alerts: Alert[] = [];

    getHeight(): number {
      if (this.alerts.length == 0) {
        return 0; 
      }
      const element = this.element.nativeElement as HTMLElement;
      return element.offsetHeight;
    }

    add(alert: Alert) {
      this.alerts.push(alert);
    }
    
  
    close(alert: Alert) {
      this.alerts.splice(this.alerts.indexOf(alert), 1);
    }
  
    reset() {
      this.alerts = [];
    }
  }