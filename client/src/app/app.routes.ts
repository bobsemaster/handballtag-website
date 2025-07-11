import {Routes} from '@angular/router';
import {TeamsView} from './teams-view/teams-view';
import {Spielplan} from './spielplan/spielplan';

export const routes: Routes = [{
  path: "",
  component: TeamsView
}, {
  path: "spielplan/:pdfName",
  component: Spielplan
}
];
