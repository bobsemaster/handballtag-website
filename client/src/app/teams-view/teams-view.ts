import {Component} from '@angular/core';
import {NgOptimizedImage} from '@angular/common';
import {RouterLink} from '@angular/router';

interface Team {
  name: string;
  description: string;
  pdfName: string;
}

@Component({
  selector: 'app-teams-view',
  imports: [
    NgOptimizedImage,
    RouterLink
  ],
  standalone: true,
  templateUrl: './teams-view.html',
  styleUrl: './teams-view.scss'
})
export class TeamsView {

  protected teams: Team[] = [{
    name: "Minis",
    description: "Samstag",
    pdfName: "Minis",
  },
    {name: "E-Jugend weiblich", "description": "Samstag", pdfName: "wE"},
    {name: "E-Jugend männlich", "description": "Samstag", pdfName: "mE"},
    {name: "D-Jugend weiblich", "description": "Samstag", pdfName: "wD"},
    {name: "D-Jugend männlich", "description": "Samstag", pdfName: "mD"},
    {name: "C-Jugend weiblich", "description": "Sonntag", pdfName: "wC"},
    {name: "C-Jugend männlich", "description": "Sonntag", pdfName: "mC"},
    {name: "B-Jugend weiblich", "description": "Sonntag", pdfName: "wB"},
    {name: "B-Jugend männlich", "description": "Sonntag", pdfName: "mB"},
    {name: "Turniermodus", "description": "", pdfName: "Turniermodus"},
    {name: "Lageplan Samstag", "description": "", pdfName: "2025_Lageplan Spielfelder Samstag"},
    {name: "Lageplan Sonntag", "description": "", pdfName: "2025_Lageplan Spielfelder Sonntag"},
  ]

  protected getPdf(team: Team): string {
    return team.pdfName
  }
}
