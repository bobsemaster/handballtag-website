import {Component, computed, input} from '@angular/core';
import {NgxExtendedPdfViewerModule} from 'ngx-extended-pdf-viewer';
import {RouterLink} from '@angular/router';
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-spielplan',
  imports: [
    NgxExtendedPdfViewerModule,
    RouterLink,
    NgOptimizedImage
  ],
  standalone: true,
  templateUrl: './spielplan.html',
  styleUrl: './spielplan.scss'
})
export class Spielplan {
  pdfName = input.required<string>()


  constructor() {

  }
}
