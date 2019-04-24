package com.young.lineloginsample

import android.os.Parcel
import android.os.Parcelable
import javax.net.ssl.SSLSocketFactory
import java.net.InetAddress
import java.net.Socket


class TLSSocketFactory() : SSLSocketFactory(), Parcelable {

    constructor(parcel: Parcel) : this() {
    }


    override fun createSocket(s: Socket?, host: String?, port: Int, autoClose: Boolean): Socket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSocket(host: String?, port: Int, localHost: InetAddress?, localPort: Int): Socket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSocket(host: InetAddress?, port: Int): Socket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSocket(address: InetAddress?, port: Int, localAddress: InetAddress?, localPort: Int): Socket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDefaultCipherSuites(): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSupportedCipherSuites(): Array<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSocket(): Socket {
        return super.createSocket()
    }

    override fun createSocket(host: String?, port: Int): Socket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TLSSocketFactory> {
        override fun createFromParcel(parcel: Parcel): TLSSocketFactory {
            return TLSSocketFactory(parcel)
        }

        override fun newArray(size: Int): Array<TLSSocketFactory?> {
            return arrayOfNulls(size)
        }
    }


}