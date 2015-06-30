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
			double stackY, final WModelIndex xIndex, final WModelIndex yIndex) {
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

	public static void setPenColor(final WPen pen, final WModelIndex xIndex,
			final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			pen.setColor((WColor) color);
		}
	}

	public static void setBrushColor(final WBrush brush,
			final WModelIndex xIndex, final WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			brush.setColor((WColor) color);
		}
	}

	private int currentXSegment_;
	private int currentYSegment_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(F,x,w,d){function Q(){return d.crosshair||d.followCurve!==-1}function la(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function j(a){if(a===g)return d.xTransform;if(a===i)return d.yTransform}function Z(){var a=m(d.area),b=o(d.area);return z([1,0,0,-1,a,b],z(j(g),z(j(i),[1,0,0,-1,-a,b])))}function C(){return z(Z(),d.area)}function S(a,b){if(b===undefined)b=false;a=b?a:z(ma(Z()), a);a=[(a[g]-d.area[0])/d.area[2],1-(a[i]-d.area[1])/d.area[3]];return[d.modelArea[0]+a[g]*d.modelArea[2],d.modelArea[1]+a[i]*d.modelArea[3]]}function O(a,b){if(b===undefined)b=false;a=[(a[g]-d.modelArea[0])/d.modelArea[2],1-(a[i]-d.modelArea[1])/d.modelArea[3]];a=[d.area[0]+a[g]*d.area[2],d.area[1]+a[i]*d.area[3]];return b?a:z(Z(),a)}function za(a,b){var e=na(a,b);if(e<0)e=0;if(e>=b.length)e=b.length-2;if(b[e][g]===a)return[a,b[e][i]];var f=e+1;if(b[f][2]==T)f+=2;return a-b[e][g]<b[f][g]-a?[b[e][g], b[e][i]]:[b[f][g],b[f][i]]}function na(a,b){function e(v){b[v][2]===oa&&--v;b[v][2]===T&&--v;return v}var f=b.length,h=Math.floor(f/2);h=e(h);var k=0,s=f,r=false;if(b[0][g]>a)return-1;if(b[f-1][g]<a)return f;for(;!r;){f=h+1;if(b[f][2]===T)f+=2;if(b[h][g]>a){s=h;h=Math.floor((s+k)/2);h=e(h)}else if(b[h][g]===a)r=true;else if(b[f][g]>a)r=true;else if(b[f][g]===a){h=f;r=true}else{k=h;h=Math.floor((s+k)/2);h=e(h)}}return h}function aa(){var a=(S([m(d.area),0])[0]-d.modelArea[0])/d.modelArea[2],b=(S([n(d.area), 0])[0]-d.modelArea[0])/d.modelArea[2],e;for(e=0;e<d.sliders.length;++e){var f=$(\"#\"+d.sliders[e]);if(f)(f=f.data(\"sobj\"))&&f.changeRange(a,b)}}function U(){J&&ba(function(){w.repaint();Q()&&pa()})}function pa(){if(J){var a=E.getContext(\"2d\");a.clearRect(0,0,E.width,E.height);a.save();a.beginPath();a.moveTo(m(d.area),q(d.area));a.lineTo(n(d.area),q(d.area));a.lineTo(n(d.area),o(d.area));a.lineTo(m(d.area),o(d.area));a.closePath();a.clip();var b=z(ma(Z()),p),e=p[g],f=p[i];if(d.followCurve!==-1){b=za(b[g], d.series[d.followCurve]);f=z(Z(),b);e=f[g];f=f[i];p[g]=e;p[i]=f}b=[(b[g]-d.area[0])/d.area[2],1-(b[i]-d.area[1])/d.area[3]];b=[d.modelArea[0]+b[g]*d.modelArea[2],d.modelArea[1]+b[i]*d.modelArea[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var h=b[0].toFixed(2);b=b[1].toFixed(2);if(h==\"-0.00\")h=\"0.00\";if(b==\"-0.00\")b=\"0.00\";a.fillText(\"(\"+h+\",\"+b+\")\",n(d.area)-5,q(d.area)+5);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(e)+0.5,Math.floor(q(d.area))+ 0.5);a.lineTo(Math.floor(e)+0.5,Math.floor(o(d.area))+0.5);a.moveTo(Math.floor(m(d.area))+0.5,Math.floor(f)+0.5);a.lineTo(Math.floor(n(d.area))+0.5,Math.floor(f)+0.5);a.stroke();a.restore()}}function V(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=m(b)&&e<=n(b)&&a>=q(b)&&a<=o(b)}function Aa(a){return q(a)<=q(d.area)+da&&o(a)>=o(d.area)-da&&m(a)<=m(d.area)+da&&n(a)>=n(d.area)-da}function G(a){var b=C();if(a===undefined||a===ea)if(j(g)[0]<1){j(g)[0]=1;b=C()}if(a===undefined|| a===fa)if(j(i)[3]<1){j(i)[3]=1;b=C()}if(a===undefined||a===ea){if(m(b)>m(d.area)){b=m(d.area)-m(b);j(g)[4]=j(g)[4]+b;b=C()}if(n(b)<n(d.area)){b=n(d.area)-n(b);j(g)[4]=j(g)[4]+b;b=C()}}if(a===undefined||a===fa){if(q(b)>q(d.area)){b=q(d.area)-q(b);j(i)[5]=j(i)[5]-b;b=C()}if(o(b)<o(d.area)){b=o(d.area)-o(b);j(i)[5]=j(i)[5]-b;C()}}}function Ba(){if(E===undefined&&Q()){c=document.createElement(\"canvas\");c.setAttribute(\"width\",w.canvas.width);c.setAttribute(\"height\",w.canvas.height);c.style.position=\"absolute\"; c.style.display=\"block\";c.style.left=\"0\";c.style.top=\"0\";c.style.msTouchAction=\"none\";w.canvas.parentNode.appendChild(c);E=c;jQuery.data(x,\"oobj\",E)}else if(E!==undefined&&!Q()){E.parentNode.removeChild(E);jQuery.removeData(x,\"oobj\");E=undefined}if(p===null)p=O([(m(d.modelArea)+n(d.modelArea))/2,(q(d.modelArea)+o(d.modelArea))/2])}function Ca(a,b){var e=Math.cos(a);a=Math.sin(a);var f=e*a,h=-b[0]*e-b[1]*a;return[e*e,f,f,a*a,e*h+b[0],a*h+b[1]]}function Da(a,b,e){a=[b[g]-a[g],b[i]-a[i]];return e*e>= a[g]*a[g]+a[i]*a[i]}function qa(a,b){if(W){var e=Date.now();if(b===undefined)b=e-H;var f={x:0,y:0},h=C(),k=Ea;if(b>2*ca){J=false;var s=Math.floor(b/ca-1),r;for(r=0;r<s;++r){qa(a,ca);if(!W){J=true;U();return}}b-=s*ca;J=true}if(l.x===Infinity||l.x===-Infinity)l.x=l.x>0?K:-K;if(isFinite(l.x)){l.x/=1+va*b;h[0]+=l.x*b;if(m(h)>m(d.area)){l.x+=-k*(m(h)-m(d.area))*b;l.x*=0.7}else if(n(h)<n(d.area)){l.x+=-k*(n(h)-n(d.area))*b;l.x*=0.7}if(Math.abs(l.x)<ra)if(m(h)>m(d.area))l.x=ra;else if(n(h)<n(d.area))l.x= -ra;if(Math.abs(l.x)>K)l.x=(l.x>0?1:-1)*K;f.x=l.x*b}if(l.y===Infinity||l.y===-Infinity)l.y=l.y>0?K:-K;if(isFinite(l.y)){l.y/=1+va*b;h[1]+=l.y*b;if(q(h)>q(d.area)){l.y+=-k*(q(h)-q(d.area))*b;l.y*=0.7}else if(o(h)<o(d.area)){l.y+=-k*(o(h)-o(d.area))*b;l.y*=0.7}if(Math.abs(l.y)<0.001)if(q(h)>q(d.area))l.y=0.001;else if(o(h)<o(d.area))l.y=-0.001;if(Math.abs(l.y)>K)l.y=(l.y>0?1:-1)*K;f.y=l.y*b}h=C();M(f,X);a=C();if(m(h)>m(d.area)&&m(a)<=m(d.area)){l.x=0;M({x:-f.x,y:0},X);G(ea)}if(n(h)<n(d.area)&&n(a)>= n(d.area)){l.x=0;M({x:-f.x,y:0},X);G(ea)}if(q(h)>q(d.area)&&q(a)<=q(d.area)){l.y=0;M({x:0,y:-f.y},X);G(fa)}if(o(h)<o(d.area)&&o(a)>=o(d.area)){l.y=0;M({x:0,y:-f.y},X);G(fa)}if(Math.abs(l.x)<wa&&Math.abs(l.y)<wa&&Aa(a)){G();W=false;B=null;l.x=0;l.y=0;H=null;t=[]}else{H=e;J&&ba(qa)}}}function xa(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1}function ga(){var a,b,e=xa(j(g)[0])-1;if(e>=d.pens.x.length)e=d.pens.x.length-1;for(a=0;a<d.pens.x.length;++a)if(e===a)for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]= d.penAlpha.x[b];else for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]=0;e=xa(j(i)[3])-1;if(e>=d.pens.y.length)e=d.pens.y.length-1;for(a=0;a<d.pens.y.length;++a)if(e===a)for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=d.penAlpha.y[b];else for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=0}function M(a,b){var e=S(p);if(b&X){j(g)[4]=j(g)[4]+a.x;j(i)[5]=j(i)[5]-a.y}else if(b&ya){b=C();if(m(b)>m(d.area)){if(a.x>0)a.x/=1+(m(b)-m(d.area))*ha}else if(n(b)<n(d.area))if(a.x<0)a.x/= 1+(n(d.area)-n(b))*ha;if(q(b)>q(d.area)){if(a.y>0)a.y/=1+(q(b)-q(d.area))*ha}else if(o(b)<o(d.area))if(a.y<0)a.y/=1+(o(d.area)-o(b))*ha;j(g)[4]=j(g)[4]+a.x;j(i)[5]=j(i)[5]-a.y;p[g]+=a.x;p[i]+=a.y}else{j(g)[4]=j(g)[4]+a.x;j(i)[5]=j(i)[5]-a.y;p[g]+=a.x;p[i]+=a.y;G()}a=O(e);p[g]=a[g];p[i]=a[i];U();aa()}function sa(a,b,e){var f=S(p),h=z(ma([1,0,0,-1,m(d.area),o(d.area)]),[a.x,a.y]);a=h[0];h=h[1];b=Math.pow(1.2,b);e=Math.pow(1.2,e);if(j(g)[0]*b>d.maxZoom[g])b=d.maxZoom[g]/j(g)[0];if(b<1||j(g)[0]!==d.maxZoom[g])ia(j(g), z([b,0,0,1,a-b*a,0],j(g)));if(j(i)[3]*e>d.maxZoom[i])e=d.maxZoom[i]/j(i)[3];if(e<1||j(i)[3]!==d.maxZoom[i])ia(j(i),z([1,0,0,e,0,h-e*h],j(i)));G();f=O(f);p[g]=f[g];p[i]=f[i];ga();U();aa()}var ca=17,ba=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,ca)}}();w.canvas.style.msTouchAction=\"none\";var T=2,oa=3,X=1,ya=2,ea=1,fa=2,g=0,i=1,I=false;if(!window.TouchEvent&&(window.MSPointerEvent||window.PointerEvent))(function(){function a(){if(pointers.length> 0&&!I)I=true;else if(pointers.length<=0&&I)I=false}function b(h){if(la(h)){h.preventDefault();pointers.push(h);a();Y.touchStart(x,{touches:pointers.slice(0)})}}function e(h){if(I)if(la(h)){h.preventDefault();var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===h.pointerId){pointers.splice(k,1);break}a();Y.touchEnd(x,{touches:pointers.slice(0),changedTouches:[]})}}function f(h){if(la(h)){h.preventDefault();var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===h.pointerId){pointers[k]= h;break}a();Y.touchMoved(x,{touches:pointers.slice(0)})}}pointers=[];if(window.PointerEvent){x.addEventListener(\"pointerdown\",b);x.addEventListener(\"pointerup\",e);x.addEventListener(\"pointerout\",e);x.addEventListener(\"pointermove\",f)}else{x.addEventListener(\"MSPointerDown\",b);x.addEventListener(\"MSPointerUp\",e);x.addEventListener(\"MSPointerOut\",e);x.addEventListener(\"MSPointerMove\",f)}})();var va=0.003,Ea=2.0E-4,ha=0.07,da=3,ra=0.001,K=1.5,wa=0.02;jQuery.data(x,\"cobj\",this);var Y=this,u=F.WT;Y.config= d;var E=jQuery.data(x,\"oobj\"),p=null,J=true,B=null,t=[],P=false,N=false,D=null,ta=null,ua=null,l={x:0,y:0},H=null,ja=null;F=u.gfxUtils;var z=F.transform_mult,ma=F.transform_inverted,ia=F.transform_assign,q=F.rect_top,o=F.rect_bottom,m=F.rect_left,n=F.rect_right,W=false;this.mouseMove=function(a,b){setTimeout(function(){if(!I){var e=u.widgetCoordinates(w.canvas,b);if(V(e,d.area))if(Q()&&J){p=[e.x,e.y];ba(pa)}}},0)};this.mouseDown=function(a,b){if(!I){a=u.widgetCoordinates(w.canvas,b);if(V(a,d.area))B= a}};this.mouseUp=function(){I||(B=null)};this.mouseDrag=function(a,b){if(!I)if(B!==null){a=u.widgetCoordinates(w.canvas,b);if(V(a,d.area)){u.buttons===1&&d.pan&&M({x:a.x-B.x,y:a.y-B.y});B=a}}};this.mouseWheel=function(a,b){var e=u.widgetCoordinates(w.canvas,b);if(V(e,d.area)){a=u.normalizeWheel(b);if(!b.ctrlKey&&d.pan){e=j(g)[4];var f=j(i)[5];M({x:-a.pixelX,y:-a.pixelY});if(e!==j(g)[4]||f!==j(i)[5])u.cancelEvent(b)}else if(b.ctrlKey&&d.zoom){u.cancelEvent(b);f=-a.spinY;if(f===0)f=-a.spinX;if(b.shiftKey&& !b.altKey)sa(e,0,f);else b.altKey&&!b.shiftKey?sa(e,f,0):sa(e,f,f)}}};this.touchStart=function(a,b){P=b.touches.length===1;N=b.touches.length===2;if(P){W=false;a=u.widgetCoordinates(w.canvas,b.touches[0]);if(!V(a,d.area))return;ja=Q()&&Da(p,[a.x,a.y],30)?1:0;H=Date.now();B=a;u.capture(null);u.capture(w.canvas)}else if(N&&d.zoom){W=false;t=[u.widgetCoordinates(w.canvas,b.touches[0]),u.widgetCoordinates(w.canvas,b.touches[1])].map(function(f){return[f.x,f.y]});if(!t.every(function(f){return V(f,d.area)})){N= null;return}u.capture(null);u.capture(w.canvas);D=Math.atan2(t[1][1]-t[0][1],t[1][0]-t[0][0]);ta=[(t[0][0]+t[1][0])/2,(t[0][1]+t[1][1])/2];a=Math.abs(Math.sin(D));var e=Math.abs(Math.cos(D));D=a<Math.sin(0.125*Math.PI)?0:e<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(D)>0?Math.PI/4:-Math.PI/4;ua=Ca(D,ta)}else return;b.preventDefault&&b.preventDefault()};this.touchEnd=function(a,b){var e=Array.prototype.slice.call(b.touches),f=e.length===0;P=e.length===1;N=e.length===2;f||function(){var h;for(h=0;h< b.changedTouches.length;++h)(function(){for(var k=b.changedTouches[h].identifier,s=0;s<e.length;++s)if(e[s].identifier===k){e.splice(s,1);return}})()}();f=e.length===0;P=e.length===1;N=e.length===2;if(f){if(ja===0&&(isFinite(l.x)||isFinite(l.y))&&d.rubberBand){H=Date.now();W=true;ba(qa)}else{Y.mouseUp(null,null);e=[];ua=ta=D=null;if(H!=null){Date.now();H=null}}ja=null}else if(P||N)Y.touchStart(a,b)};this.touchMoved=function(a,b){if(P||N)if(P){if(B!==null){a=u.widgetCoordinates(w.canvas,b.touches[0]); var e=Date.now(),f={x:a.x-B.x,y:a.y-B.y},h=e-H;H=e;if(ja===1){p[g]+=f.x;p[i]+=f.y;Q()&&J&&ba(pa)}else if(d.pan){if(a.x<d.area[0]||a.x>d.area[0]+d.area[2]){l={x:0,y:0};return}if(a.y<d.area[1]||a.y>d.area[1]+d.area[3]){l={x:0,y:0};return}l.x=f.x/h;l.y=f.y/h;M(f,d.rubberBand?ya:0)}b.preventDefault&&b.preventDefault();B=a}}else if(N&&d.zoom){b.preventDefault&&b.preventDefault();a=S(p);var k=(t[0][0]+t[1][0])/2,s=(t[0][1]+t[1][1])/2;b=[u.widgetCoordinates(w.canvas,b.touches[0]),u.widgetCoordinates(w.canvas, b.touches[1])].map(function(ka){return D===0?[ka.x,s]:D===Math.PI/2?[k,ka.y]:z(ua,[ka.x,ka.y])});e=Math.abs(t[1][0]-t[0][0]);f=Math.abs(b[1][0]-b[0][0]);h=e>0?f/e:1;if(f===e||D===Math.PI/2)h=1;if(j(g)[0]*h>d.maxZoom[g])h=d.maxZoom[g]/j(g)[0];var r=(b[0][0]+b[1][0])/2,v=Math.abs(t[1][1]-t[0][1]),A=Math.abs(b[1][1]-b[0][1]),y=v?A/v:1;if(A===v||D===0)y=1;if(j(i)[3]*y>d.maxZoom[i])y=d.maxZoom[i]/j(i)[3];var Fa=(b[0][1]+b[1][1])/2;if(f!=e&&(h<1||j(g)[0]!==d.maxZoom[g]))ia(j(g),z([h,0,0,1,-h*k+r,0],j(g))); if(A!=v&&(y<1||j(i)[3]!==d.maxZoom[i]))ia(j(i),z([1,0,0,y,0,-y*s+Fa],j(i)));G();a=O(a);p[g]=a[g];p[i]=a[i];t=b;ga();U();aa()}};this.setXRange=function(a,b,e){b=d.modelArea[0]+d.modelArea[2]*b;e=d.modelArea[0]+d.modelArea[2]*e;if(b<m(d.modelArea))b=m(d.modelArea);if(e>n(d.modelArea))e=n(d.modelArea);var f=d.series[a];if(f.length!==0){a=O([b,0],true);var h=O([e,0],true),k=na(a[g],f);if(k<0)k=0;else{k++;if(f[k][2]===T)k+=2}var s=na(h[g],f),r,v,A=Infinity,y=-Infinity;for(r=k;r<=s&&r<f.length;++r)if(f[r][2]!== T&&f[r][2]!==oa){if(f[r][i]<A)A=f[r][i];if(f[r][i]>y)y=f[r][i]}if(k>0){v=k-1;if(f[v][2]===oa)v-=2;r=(a[g]-f[v][g])/(f[k][g]-f[v][g]);k=f[v][i]+r*(f[k][i]-f[v][i]);if(k<A)A=k;if(k>y)y=k}if(s<f.length-1){k=s+1;if(f[k][2]===T)k+=2;r=(h[g]-f[s][g])/(f[k][g]-f[s][g]);k=f[s][i]+r*(f[k][i]-f[s][i]);if(k<A)A=k;if(k>y)y=k}b=d.modelArea[2]/(e-b);e=d.area[3]/(y-A);e=d.area[3]/(d.area[3]/e+20);if(e>d.maxZoom[i])e=d.maxZoom[i];a=[a[g]-m(d.area),-((A+y)/2+d.area[3]/e/2-o(d.area))];A=S(p);j(g)[0]=b;j(i)[3]=e;j(g)[4]= -a[g]*b;j(i)[5]=-a[i]*e;a=O(A);p[g]=a[g];p[i]=a[i];G();ga();U();aa()}};this.getSeries=function(a){return d.series[a]};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))d[b]=a[b];Ba();ga();U();aa()};this.updateConfig({})}");
	}

	private static final int TICK_LENGTH = 5;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
