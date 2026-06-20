import {Routes} from '@angular/router';

export const routes: Routes = [{
  path: "",
  loadComponent: () => import('./teams-view/teams-view').then((m) => m.TeamsView)
}, {
  path: "spielplan/:pdfName",
  loadComponent: () => import('./spielplan/spielplan').then((m) => m.Spielplan)
}
];
