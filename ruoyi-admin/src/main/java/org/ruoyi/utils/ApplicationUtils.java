package org.ruoyi.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 小工具
 */
@Slf4j
public class ApplicationUtils {

    public static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = resolveExternalHostAddress();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles());
    }

    /**
     * 尽量稳定地获取“局域网可访问”的 IP。
     * <p>
     * InetAddress.getLocalHost() 在多网卡/VPN/hosts 配置下经常会返回 127.0.0.1，
     * 因此这里优先从网卡枚举中挑选第一个可用的内网 IPv4（site-local）。
     */
    private static String resolveExternalHostAddress() {
        try {
            InetAddress lan = findFirstLanIPv4();
            if (lan != null) {
                return lan.getHostAddress();
            }
        } catch (Exception e) {
            log.warn("Failed to resolve LAN IPv4 address, fallback to localhost: {}", e.getMessage());
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
            return "localhost";
        }
    }

    private static InetAddress findFirstLanIPv4() throws SocketException {
        InetAddress candidate = null;
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) {
            return null;
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isUp() || ni.isLoopback()) {
                continue;
            }
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                if (addr.isLoopbackAddress() || !(addr instanceof Inet4Address)) {
                    continue;
                }
                // 192.168.x.x / 10.x.x.x / 172.16-31.x.x
                if (addr.isSiteLocalAddress()) {
                    return addr;
                }
                // 兜底：保留一个“非 link-local”的 IPv4 作为候选
                if (candidate == null && !addr.isLinkLocalAddress()) {
                    candidate = addr;
                }
            }
        }
        return candidate;
    }

}
