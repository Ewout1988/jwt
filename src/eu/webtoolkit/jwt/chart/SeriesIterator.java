/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for iterating over series data in a chart.
 * <p>
 * 
 * This class is specialized for rendering series data.
 * <p>
 */
public class SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(SeriesIterator.class);

	/**
	 * Start handling a new segment.
	 * <p>
	 * Because of a &apos;break&apos; specified in an axis, axes may be divided
	 * in one or two segments (in fact only the API limits this now to two). The
	 * iterator will iterate all segments seperately, but each time with a
	 * different clipping region specified in the painter, corresponding to that
	 * segment.
	 * <p>
	 * The <i>currentSegmentArea</i> specifies the clipping area.
	 */
	public void startSegment(int currentXSegment, int currentYSegment,
			final WRectF currentSegmentArea) {
		this.currentXSegment_ = currentXSegment;
		this.currentYSegment_ = currentYSegment;
	}

	/**
	 * End handling a particular segment.
	 * <p>
	 * 
	 * @see SeriesIterator#startSegment(int currentXSegment, int
	 *      currentYSegment, WRectF currentSegmentArea)
	 */
	public void endSegment() {
	}

	/**
	 * Start iterating a particular series.
	 * <p>
	 * Returns whether the series values should be iterated. The
	 * <i>groupWidth</i> is the width (in pixels) of a single bar group. The
	 * chart contains <i>numBarGroups</i>, and the current series is in the
	 * <i>currentBarGroup</i>&apos;th group.
	 */
	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return true;
	}

	/**
	 * End iterating a particular series.
	 */
	public void endSeries() {
	}

	/**
	 * Process a value.
	 * <p>
	 * Processes a value with model coordinates (<i>x</i>, <i>y</i>). The y
	 * value may differ from the model&apos;s y value, because of stacked
	 * series. The y value here corresponds to the location on the chart, after
	 * stacking.
	 * <p>
	 * The <i>stackY</i> argument is the y value from the previous series (also
	 * after stacking). It will be 0, unless this series is stacked.
	 */
	public void newValue(final WDataSeries series, double x, double y,
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
	}

	/**
	 * Returns the current X segment.
	 */
	public int getCurrentXSegment() {
		return this.currentXSegment_;
	}

	/**
	 * Returns the current Y segment.
	 */
	public int getCurrentYSegment() {
		return this.currentYSegment_;
	}

	public static void setPenColor(final WPen pen, final WDataSeries series,
			int xRow, int xColumn, int yRow, int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerPenColorRole) {
				color = series.getModel().getMarkerPenColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.MarkerBrushColorRole) {
					color = series.getModel()
							.getMarkerBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			pen.setColor(color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WDataSeries series, int xRow, int xColumn, int yRow,
			int yColumn, int colorRole) {
		WColor color = null;
		if (yRow >= 0 && yColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(yRow, yColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(yRow, yColumn);
				}
			}
		}
		if (!(color != null) && xRow >= 0 && xColumn >= 0) {
			if (colorRole == ItemDataRole.MarkerBrushColorRole) {
				color = series.getModel().getMarkerBrushColor(xRow, xColumn);
			} else {
				if (colorRole == ItemDataRole.BarBrushColorRole) {
					color = series.getModel().getBarBrushColor(xRow, xColumn);
				}
			}
		}
		if (color != null) {
			brush.setColor(color);
		}
		;
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(v){function z(a,b,e,c){function d(f){return e?b[f]:b[l-1-f]}function j(f){for(;d(f)[2]===t||d(f)[2]===w;)f--;return f}var q=h;if(c)q=i;var l=b.length;c=Math.floor(l/2);c=j(c);var x=0,m=l,k=false;if(d(0)[q]>a)return e?-1:l;if(d(l-1)[q]<a)return e?l:-1;for(;!k;){var g=c+1;if(g<l&&(d(g)[2]===t||d(g)[2]===w))g+=2;if(d(c)[q]>a){m=c;c=Math.floor((m+x)/2);c=j(c)}else if(d(c)[q]===a)k=true;else if(g<l&&d(g)[q]>a)k=true;else if(g<l&&d(g)[q]=== a){c=g;k=true}else{x=c;c=Math.floor((m+x)/2);c=j(c)}}return e?c:l-1-c}function C(a,b){return b[0][a]<b[b.length-1][a]}var t=2,w=3,h=0,i=1,A=this;v=v.WT.gfxUtils;var D=v.rect_top,E=v.rect_bottom,B=v.rect_left,F=v.rect_right,G=v.transform_mult;this.findClosestPoint=function(a,b,e){var c=h;if(e)c=i;var d=C(c,b);e=z(a,b,d,e);if(e<0)e=0;if(e>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(e>=b.length)e=b.length-2;if(b[e][c]===a)return[b[e][h],b[e][i]];var j=d?e+1:e-1;if(d&&b[j][2]==t)j+=2;if(!d&& j<0)return[b[e][h],b[e][i]];if(!d&&j>0&&b[j][2]==w)j-=2;d=Math.abs(a-b[e][c]);a=Math.abs(b[j][c]-a);return d<a?[b[e][h],b[e][i]]:[b[j][h],b[j][i]]};this.minMaxY=function(a,b){b=b?h:i;for(var e=a[0][b],c=a[0][b],d=1;d<a.length;++d)if(a[d][2]!==t&&a[d][2]!==w&&a[d][2]!==5){if(a[d][b]>c)c=a[d][b];if(a[d][b]<e)e=a[d][b]}return[e,c]};this.projection=function(a,b){var e=Math.cos(a);a=Math.sin(a);var c=e*a,d=-b[0]*e-b[1]*a;return[e*e,c,c,a*a,e*d+b[0],a*d+b[1]]};this.distanceSquared=function(a,b){a=[b[h]- a[h],b[i]-a[i]];return a[h]*a[h]+a[i]*a[i]};this.distanceLessThanRadius=function(a,b,e){return e*e>=A.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=B(b)&&e<=F(b)&&a>=D(b)&&a<=E(b)};this.toDisplayCoord=function(a,b,e,c,d){if(e){a=[(a[h]-d[0])/d[2],(a[i]-d[1])/d[3]];c=[c[0]+a[i]*c[2],c[1]+a[h]*c[3]]}else{a=[(a[h]-d[0])/d[2],1-(a[i]-d[1])/d[3]];c=[c[0]+ a[h]*c[2],c[1]+a[i]*c[3]]}return G(b,c)};this.findYRange=function(a,b,e,c,d,j,q){if(a.length!==0){var l=A.toDisplayCoord([b,0],[1,0,0,1,0,0],c,d,j),x=A.toDisplayCoord([e,0],[1,0,0,1,0,0],c,d,j),m=c?i:h,k=c?h:i,g=C(m,a),f=z(l[m],a,g,c),n=z(x[m],a,g,c),o,p,r=Infinity,s=-Infinity,y=f===n&&f===a.length||f===-1&&n===-1;if(!y){if(g)if(f<0)f=0;else{f++;if(a[f]&&a[f][2]===t)f+=2}else if(f>=a.length-1)f=a.length-2;if(!g&&n<0)n=0;for(o=Math.min(f,n);o<=Math.max(f,n)&&o<a.length;++o)if(a[o][2]!==t&&a[o][2]!== w){if(a[o][k]<r)r=a[o][k];if(a[o][k]>s)s=a[o][k]}if(g&&f>0||!g&&f<a.length-1){if(g){p=f-1;if(a[p]&&a[p][2]===w)p-=2}else{p=f+1;if(a[p]&&a[p][2]===t)p+=2}o=(l[m]-a[p][m])/(a[f][m]-a[p][m]);f=a[p][k]+o*(a[f][k]-a[p][k]);if(f<r)r=f;if(f>s)s=f}if(g&&n<a.length-1||!g&&n>0){if(g){g=n+1;if(a[g][2]===t)g+=2}else{g=n-1;if(a[g][2]===w)g-=2}o=(x[m]-a[n][m])/(a[g][m]-a[n][m]);f=a[n][k]+o*(a[g][k]-a[n][k]);if(f<r)r=f;if(f>s)s=f}}var u;a=j[2]/(e-b);b=c?2:3;if(!y){u=d[b]/(s-r);u=d[b]/(d[b]/u+20);if(u>q[k])u=q[k]}c= c?[l[i]-D(d),!y?(r+s)/2-d[2]/u/2-B(d):0]:[l[h]-B(d),!y?-((r+s)/2+d[3]/u/2-E(d)):0];return{xZoom:a,yZoom:u,panPoint:c}}}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(la,D,u,k){function J(a){return a===undefined}function m(){return k.modelArea}function Ea(){return k.followCurve}function ma(){return k.crosshair||Ea()!==-1}function y(){return k.isHorizontal}function e(a){if(a===g)return k.xTransform;if(a===f)return k.yTransform}function i(){return k.area}function l(){return k.insideArea}function na(a){return J(a)?k.series:k.series[a]}function S(a){return na(a).transform}function $a(a){return y()? z([0,1,1,0,0,0],z(S(a),[0,1,1,0,0,0])):S(a)}function Fa(a){return na(a).curve}function ab(){return k.seriesSelection}function bb(){return k.sliders}function cb(){return k.hasToolTips}function db(){return k.coordinateOverlayPadding}function ua(){return k.curveManipulation}function G(){return k.maxZoom}function F(){return k.pens}function Ga(){return k.selectedCurve}function oa(a){a.preventDefault&&a.preventDefault()}function T(a,b){D.addEventListener(a,b)}function N(a,b){D.removeEventListener(a,b)} function A(a){return a.length}function Pa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ha(){if(n){if(n.tooltipTimeout){clearTimeout(n.tooltipTimeout);n.tooltipTimeout=null}if(!n.overTooltip)if(n.tooltipOuterDiv){document.body.removeChild(n.tooltipOuterDiv);n.tooltipEl=null;n.tooltipOuterDiv=null}}}function va(){if(k.notifyTransform.x||k.notifyTransform.y){if(Ia){window.clearTimeout(Ia);Ia=null}Ia=setTimeout(function(){if(k.notifyTransform.x&& !eb(Qa,e(g))){la.emit(u.widget,\"xTransformChanged\");Y(Qa,e(g))}if(k.notifyTransform.y&&!eb(Ra,e(f))){la.emit(u.widget,\"yTransformChanged\");Y(Ra,e(f))}},nb)}}function Z(){var a,b;if(y()){a=o(i());b=t(i());return z([0,1,1,0,a,b],z(e(g),z(e(f),[0,1,1,0,-b,-a])))}else{a=o(i());b=v(i());return z([1,0,0,-1,a,b],z(e(g),z(e(f),[1,0,0,-1,-a,b])))}}function K(){return z(Z(),l())}function aa(a,b){if(J(b))b=false;a=b?a:z(wa(Z()),a);a=y()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]- i()[1])/i()[3]];return[m()[0]+a[g]*m()[2],m()[1]+a[f]*m()[3]]}function xa(a,b){if(J(b))b=false;return ba.toDisplayCoord(a,b?[1,0,0,1,0,0]:Z(),y(),i(),m())}function ya(){var a,b;if(y()){a=(aa([0,t(i())])[0]-m()[0])/m()[2];b=(aa([0,v(i())])[0]-m()[0])/m()[2]}else{a=(aa([o(i()),0])[0]-m()[0])/m()[2];b=(aa([q(i()),0])[0]-m()[0])/m()[2]}var c;for(c=0;c<A(bb());++c){var h=$(\"#\"+bb()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function O(){Ha();if(cb()&&n.tooltipPosition)n.tooltipTimeout=setTimeout(function(){fb()}, gb);ca&&hb(function(){u.repaint();ma()&&Sa()})}function Sa(){if(ca){var a=E.getContext(\"2d\");a.clearRect(0,0,E.width,E.height);a.save();a.beginPath();a.moveTo(o(i()),t(i()));a.lineTo(q(i()),t(i()));a.lineTo(q(i()),v(i()));a.lineTo(o(i()),v(i()));a.closePath();a.clip();var b=z(wa(Z()),w),c=w[g],h=w[f];if(Ea()!==-1){b=ob(y()?b[f]:b[g],Fa(Ea()),y());h=z(Z(),z($a(Ea()),b));c=h[g];h=h[f];w[g]=c;w[f]=h}b=y()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[m()[0]+ b[g]*m()[2],m()[1]+b[f]*m()[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",q(i())-db()[0],t(i())+db()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(v(i()))+0.5);a.moveTo(Math.floor(o(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(q(i()))+0.5,Math.floor(h)+0.5);a.stroke(); a.restore()}}function pb(a){return t(a)<=t(i())+Ja&&v(a)>=v(i())-Ja&&o(a)<=o(i())+Ja&&q(a)>=q(i())-Ja}function U(a){var b=K();if(y())if(a===pa)a=qa;else if(a===qa)a=pa;if(J(a)||a===pa)if(e(g)[0]<1){e(g)[0]=1;b=K()}if(J(a)||a===qa)if(e(f)[3]<1){e(f)[3]=1;b=K()}if(J(a)||a===pa){if(o(b)>o(l())){b=o(l())-o(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}if(q(b)<q(l())){b=q(l())-q(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}}if(J(a)||a===qa){if(t(b)>t(l())){b=t(l())-t(b);if(y())e(g)[4]= e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b=K()}if(v(b)<v(l())){b=v(l())-v(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;K()}}va()}function fb(){la.emit(u.widget,\"loadTooltip\",n.tooltipPosition[g],n.tooltipPosition[f])}function qb(){if(ma()&&(J(E)||u.canvas.width!==E.width||u.canvas.height!==E.height)){if(E){E.parentNode.removeChild(E);jQuery.removeData(D,\"oobj\");E=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",u.canvas.width);a.setAttribute(\"height\",u.canvas.height);a.style.position= \"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}u.canvas.parentNode.appendChild(a);E=a;jQuery.data(D,\"oobj\",E)}else if(!J(E)&&!ma()){E.parentNode.removeChild(E);jQuery.removeData(D,\"oobj\");E=undefined}w||(w=xa([(o(m())+q(m()))/2,(t(m())+v(m()))/2]))}function Ta(a,b){if(ra){var c=Date.now();if(J(b))b=c-V;var h={x:0,y:0},d=K(),p=rb;if(b>2*za){ca=false;var r=Math.floor(b/za-1),s; for(s=0;s<r;++s){Ta(a,za);if(!ra){ca=true;O();return}}b-=r*za;ca=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?da:-da;if(isFinite(j.x)){j.x/=1+ib*b;d[0]+=j.x*b;if(o(d)>o(l())){j.x+=-p*(o(d)-o(l()))*b;j.x*=0.7}else if(q(d)<q(l())){j.x+=-p*(q(d)-q(l()))*b;j.x*=0.7}if(Math.abs(j.x)<Ua)if(o(d)>o(l()))j.x=Ua;else if(q(d)<q(l()))j.x=-Ua;if(Math.abs(j.x)>da)j.x=(j.x>0?1:-1)*da;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?da:-da;if(isFinite(j.y)){j.y/=1+ib*b;d[1]+=j.y*b;if(t(d)>t(l())){j.y+= -p*(t(d)-t(l()))*b;j.y*=0.7}else if(v(d)<v(l())){j.y+=-p*(v(d)-v(l()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(t(d)>t(l()))j.y=0.001;else if(v(d)<v(l()))j.y=-0.001;if(Math.abs(j.y)>da)j.y=(j.y>0?1:-1)*da;h.y=j.y*b}d=K();P(h,sa);a=K();if(o(d)>o(l())&&o(a)<=o(l())){j.x=0;P({x:-h.x,y:0},sa);U(pa)}if(q(d)<q(l())&&q(a)>=q(l())){j.x=0;P({x:-h.x,y:0},sa);U(pa)}if(t(d)>t(l())&&t(a)<=t(l())){j.y=0;P({x:0,y:-h.y},sa);U(qa)}if(v(d)<v(l())&&v(a)>=v(l())){j.y=0;P({x:0,y:-h.y},sa);U(qa)}if(Math.abs(j.x)<jb&&Math.abs(j.y)< jb&&pb(a)){U();ra=false;B=null;j.x=0;j.y=0;V=null;x=[]}else{V=c;ca&&Ka(Ta)}}}function La(){var a,b,c=kb(e(g)[0])-1;if(c>=A(F().x))c=A(F().x)-1;for(a=0;a<A(F().x);++a)if(c===a)for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=k.penAlpha.x[b];else for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=0;c=kb(e(f)[3])-1;if(c>=A(F().y))c=A(F().y)-1;for(a=0;a<A(F().y);++a)if(c===a)for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]=k.penAlpha.y[b];else for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]=0}function P(a,b){if(J(b))b= 0;var c=aa(w);if(y())a={x:a.y,y:-a.x};if(b&sa){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;va()}else if(b&lb){b=K();if(o(b)>o(l())){if(a.x>0)a.x/=1+(o(b)-o(l()))*Ma}else if(q(b)<q(l()))if(a.x<0)a.x/=1+(q(l())-q(b))*Ma;if(t(b)>t(l())){if(a.y>0)a.y/=1+(t(b)-t(l()))*Ma}else if(v(b)<v(l()))if(a.y<0)a.y/=1+(v(l())-v(b))*Ma;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;va()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;U()}a=xa(c);w[g]=a[g];w[f]=a[f];O();ya()}function Aa(a, b,c){var h=aa(w),d;d=y()?[a.y-t(i()),a.x-o(i())]:z(wa([1,0,0,-1,o(i()),v(i())]),[a.x,a.y]);a=d[0];d=d[1];var p=Math.pow(1.2,y()?c:b);b=Math.pow(1.2,y()?b:c);if(e(g)[0]*p>G()[g])p=G()[g]/e(g)[0];if(p<1||e(g)[0]!==G()[g])Na(e(g),z([p,0,0,1,a-p*a,0],e(g)));if(e(f)[3]*b>G()[f])b=G()[f]/e(f)[3];if(b<1||e(f)[3]!==G()[f])Na(e(f),z([1,0,0,b,0,d-b*d],e(f)));U();h=xa(h);w[g]=h[g];w[f]=h[f];La();O();ya()}jQuery.data(D,\"cobj\",this);var ea=this,C=la.WT;ea.config=k;var H=C.gfxUtils,z=H.transform_mult,wa=H.transform_inverted, Y=H.transform_assign,eb=H.transform_equal,sb=H.transform_apply,t=H.rect_top,v=H.rect_bottom,o=H.rect_left,q=H.rect_right,ba=C.chartCommon,tb=ba.minMaxY,ob=ba.findClosestPoint,ub=ba.projection,mb=ba.distanceLessThanRadius,kb=ba.toZoomLevel,ta=ba.isPointInRect,vb=ba.findYRange,za=17,Ka=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,za)}}(),Va=false,hb=function(a){if(!Va){Va=true;Ka(function(){a();Va= false})}};if(window.MSPointerEvent||window.PointerEvent){D.style.touchAction=\"none\";u.canvas.style.msTouchAction=\"none\";u.canvas.style.touchAction=\"none\"}var sa=1,lb=2,pa=1,qa=2,g=0,f=1,nb=250,gb=500,ib=0.003,rb=2.0E-4,Ma=0.07,Ja=3,Ua=0.001,da=1.5,jb=0.02,ga=jQuery.data(D,\"eobj2\");if(!ga){ga={};ga.contextmenuListener=function(a){oa(a);N(\"contextmenu\",ga.contextmenuListener)}}jQuery.data(D,\"eobj2\",ga);var Q={},ha=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ha=A(d)> 0}function b(r){if(Pa(r)){oa(r);d.push(r);a();Q.start(D,{touches:d.slice(0)})}}function c(r){if(ha)if(Pa(r)){oa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d.splice(s,1);break}a();Q.end(D,{touches:d.slice(0),changedTouches:[]})}}function h(r){if(Pa(r)){oa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d[s]=r;break}a();Q.moved(D,{touches:d.slice(0)})}}var d=[],p=jQuery.data(D,\"eobj\");if(p)if(window.PointerEvent){N(\"pointerdown\",p.pointerDown);N(\"pointerup\",p.pointerUp); N(\"pointerout\",p.pointerUp);N(\"pointermove\",p.pointerMove)}else{N(\"MSPointerDown\",p.pointerDown);N(\"MSPointerUp\",p.pointerUp);N(\"MSPointerOut\",p.pointerUp);N(\"MSPointerMove\",p.pointerMove)}jQuery.data(D,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h});if(window.PointerEvent){T(\"pointerdown\",b);T(\"pointerup\",c);T(\"pointerout\",c);T(\"pointermove\",h)}else{T(\"MSPointerDown\",b);T(\"MSPointerUp\",c);T(\"MSPointerOut\",c);T(\"MSPointerMove\",h)}})();var E=jQuery.data(D,\"oobj\"),w=null,ca=true,B=null,x=[],W=false, fa=false,M=null,Wa=null,Xa=null,j={x:0,y:0},ia=null,V=null,n=jQuery.data(D,\"tobj\");if(!n){n={overTooltip:false};jQuery.data(D,\"tobj\",n)}var Ba=null,ra=false,Ia=null,Qa=[0,0,0,0,0,0];Y(Qa,e(g));var Ra=[0,0,0,0,0,0];Y(Ra,e(f));var Na=function(a,b){Y(a,b);va()};u.combinedTransform=Z;this.updateTooltip=function(a){Ha();if(a)if(n.tooltipPosition){n.toolTipEl=document.createElement(\"div\");n.toolTipEl.className=k.ToolTipInnerStyle;n.toolTipEl.innerHTML=a;n.tooltipOuterDiv=document.createElement(\"div\");n.tooltipOuterDiv.className= k.ToolTipOuterStyle;document.body.appendChild(n.tooltipOuterDiv);n.tooltipOuterDiv.appendChild(n.toolTipEl);var b=C.widgetPageCoordinates(u.canvas);a=n.tooltipPosition[g]+b.x;b=n.tooltipPosition[f]+b.y;C.fitToWindow(n.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(n.toolTipEl).mouseenter(function(){n.overTooltip=true});$(n.toolTipEl).mouseleave(function(){n.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ha,200);if(!ha){var c=C.widgetCoordinates(u.canvas,b);if(ta(c,i())){if(!n.tooltipEl&& cb()){n.tooltipPosition=[c.x,c.y];n.tooltipTimeout=setTimeout(function(){fb()},gb)}if(ma()&&ca){w=[c.x,c.y];hb(Sa)}}}},0)};this.mouseOut=function(){setTimeout(Ha,200)};this.mouseDown=function(a,b){if(!ha){a=C.widgetCoordinates(u.canvas,b);if(ta(a,i()))B=a}};this.mouseUp=function(){ha||(B=null)};this.mouseDrag=function(a,b){if(!ha)if(B!==null){a=C.widgetCoordinates(u.canvas,b);if(ta(a,i())){if(C.buttons===1)if(ua()){b=Ga();if(na(b)){var c;c=y()?a.x-B.x:a.y-B.y;Y(S(b),z([1,0,0,1,0,c/e(f)[3]],S(b))); O()}}else k.pan&&P({x:a.x-B.x,y:a.y-B.y});B=a}}};this.clicked=function(a,b){if(!ha)if(B===null)if(ab()){a=C.widgetCoordinates(u.canvas,b);la.emit(u.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=k.wheelActions[a];if(!J(c)){var h=C.widgetCoordinates(u.canvas,b);if(ta(h,i())){var d=C.normalizeWheel(b);if(a===0&&ua()){c=Ga();a=-d.spinY;if(na(c)){d=$a(c);d=sb(d,Fa(c));d=tb(d,y());d=(d[0]+d[1])/2;C.cancelEvent(b);b=Math.pow(1.2, a);Y(S(c),z([1,0,0,b,0,d-b*d],S(c)));O()}}else if((c===4||c===5||c===6)&&k.pan){a=e(g)[4];h=e(f)[5];if(c===6)P({x:-d.pixelX,y:-d.pixelY});else if(c===5)P({x:0,y:-d.pixelX-d.pixelY});else c===4&&P({x:-d.pixelX-d.pixelY,y:0});if(a!==e(g)[4]||h!==e(f)[5])C.cancelEvent(b)}else if(k.zoom){C.cancelEvent(b);a=-d.spinY;if(a===0)a=-d.spinX;if(c===1)Aa(h,0,a);else if(c===0)Aa(h,a,0);else if(c===2)Aa(h,a,a);else if(c===3)d.pixelX!==0?Aa(h,a,0):Aa(h,0,a)}}}};var wb=function(){ab()&&la.emit(u.widget,\"seriesSelected\", B.x,B.y)};Q.start=function(a,b,c){W=A(b.touches)===1;fa=A(b.touches)===2;if(W){ra=false;a=C.widgetCoordinates(u.canvas,b.touches[0]);if(!ta(a,i()))return;Ba=ma()&&mb(w,[a.x,a.y],30)?1:0;V=Date.now();B=a;if(Ba!==1){c||(ia=window.setTimeout(wb,200));T(\"contextmenu\",ga.contextmenuListener)}C.capture(null);C.capture(u.canvas)}else if(fa&&(k.zoom||ua())){ra=false;x=[C.widgetCoordinates(u.canvas,b.touches[0]),C.widgetCoordinates(u.canvas,b.touches[1])].map(function(h){return[h.x,h.y]});if(!x.every(function(h){return ta(h, i())})){fa=null;return}C.capture(null);C.capture(u.canvas);M=Math.atan2(x[1][1]-x[0][1],x[1][0]-x[0][0]);Wa=[(x[0][0]+x[1][0])/2,(x[0][1]+x[1][1])/2];c=Math.abs(Math.sin(M));a=Math.abs(Math.cos(M));M=c<Math.sin(0.125*Math.PI)?0:a<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(M)>0?Math.PI/4:-Math.PI/4;Xa=ub(M,Wa)}else return;oa(b)};Q.end=function(a,b){if(ia){window.clearTimeout(ia);ia=null}window.setTimeout(function(){N(\"contextmenu\",ga.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches), h=A(c)===0;h||function(){var d;for(d=0;d<A(b.changedTouches);++d)(function(){for(var p=b.changedTouches[d].identifier,r=0;r<A(c);++r)if(c[r].identifier===p){c.splice(r,1);return}})()}();h=A(c)===0;W=A(c)===1;fa=A(c)===2;if(h){Oa=null;if(Ba===0&&(isFinite(j.x)||isFinite(j.y))&&k.rubberBand){V=Date.now();ra=true;Ka(Ta)}else{ea.mouseUp(null,null);c=[];Xa=Wa=M=null;if(V!=null){Date.now();V=null}}Ba=null}else if(W||fa)Q.start(a,b,true)};var Oa=null,ja=null,Ya=null;Q.moved=function(a,b){if(W||fa)if(!(W&& B==null)){oa(b);ja=C.widgetCoordinates(u.canvas,b.touches[0]);if(A(b.touches)>1)Ya=C.widgetCoordinates(u.canvas,b.touches[1]);if(W&&ia&&!mb([ja.x,ja.y],[B.x,B.y],3)){window.clearTimeout(ia);ia=null}Oa||(Oa=setTimeout(function(){if(W&&ua()){var c=Ga();if(na(c)){var h=ja,d;d=y()?(h.x-B.x)/e(f)[3]:(h.y-B.y)/e(f)[3];S(c)[5]+=d;B=h;O()}}else if(W){h=ja;d=Date.now();c={x:h.x-B.x,y:h.y-B.y};var p=d-V;V=d;if(Ba===1){w[g]+=c.x;w[f]+=c.y;ma()&&ca&&Ka(Sa)}else if(k.pan){j.x=c.x/p;j.y=c.y/p;P(c,k.rubberBand? lb:0)}B=h}else if(fa&&ua()){var r=y()?g:f;d=[ja,Ya].map(function(I){return y()?[I.x,ka]:[Ca,I.y]});c=Math.abs(x[1][r]-x[0][r]);p=Math.abs(d[1][r]-d[0][r]);var s=c>0?p/c:1;if(p===c)s=1;var ka=z(wa(Z()),[0,(x[0][r]+x[1][r])/2])[1],Da=z(wa(Z()),[0,(d[0][r]+d[1][r])/2])[1];c=Ga();if(na(c)){Y(S(c),z([1,0,0,s,0,-s*ka+Da],S(c)));B=h;O();x=d}}else if(fa&&k.zoom){h=aa(w);var Ca=(x[0][0]+x[1][0])/2;ka=(x[0][1]+x[1][1])/2;d=[ja,Ya].map(function(I){return M===0?[I.x,ka]:M===Math.PI/2?[Ca,I.y]:z(Xa,[I.x,I.y])}); c=Math.abs(x[1][0]-x[0][0]);p=Math.abs(d[1][0]-d[0][0]);var X=c>0?p/c:1;if(p===c||M===Math.PI/2)X=1;var Za=(d[0][0]+d[1][0])/2;c=Math.abs(x[1][1]-x[0][1]);p=Math.abs(d[1][1]-d[0][1]);s=c>0?p/c:1;if(p===c||M===0)s=1;Da=(d[0][1]+d[1][1])/2;y()&&function(){var I=X;X=s;s=I;I=Za;Za=Da;Da=I;I=Ca;Ca=ka;ka=I}();if(e(g)[0]*X>G()[g])X=G()[g]/e(g)[0];if(e(f)[3]*s>G()[f])s=G()[f]/e(f)[3];if(X!==1&&(X<1||e(g)[0]!==G()[g]))Na(e(g),z([X,0,0,1,-X*Ca+Za,0],e(g)));if(s!==1&&(s<1||e(f)[3]!==G()[f]))Na(e(f),z([1,0,0, s,0,-s*ka+Da],e(f)));U();h=xa(h);w[g]=h[g];w[f]=h[f];x=d;La();O();ya()}Oa=null},1))}};this.setXRange=function(a,b,c){b=m()[0]+m()[2]*b;c=m()[0]+m()[2]*c;if(o(m())>q(m())){if(b>o(m()))b=o(m());if(c<q(m()))c=q(m())}else{if(b<o(m()))b=o(m());if(c>q(m()))c=q(m())}a=Fa(a);a=vb(a,b,c,y(),i(),m(),G());b=a.xZoom;c=a.yZoom;a=a.panPoint;var h=aa(w);e(g)[0]=b;if(c)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c)e(f)[5]=-a[f]*c;va();b=xa(h);w[g]=b[g];w[f]=b[f];U();La();O();ya()};this.getSeries=function(a){return Fa(a)};this.rangeChangedCallbacks= [];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))k[b]=a[b];qb();La();O();ya()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ea.touchStart=Q.start;ea.touchEnd=Q.end;ea.touchMoved=Q.moved}else{H=function(){};ea.touchStart=H;ea.touchEnd=H;ea.touchMoved=H}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
